/*
 *
 *   Copyright 2016 Walmart Technology
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.walmart.gatling.commons;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.dispatch.ExecutionContexts;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import scala.concurrent.ExecutionContextExecutorService;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static akka.dispatch.Futures.*;

/**
 * Created by ahailemichael on 8/17/15.
 */
public class JarExecutor extends WorkExecutor {

    public static final String SIMULATION = "simulation";
    public static final String DATA = "data";
    public static final String BODIES = "bodies";
    
    IOFileFilter logFilter = new IOFileFilter() {
        @Override
        public boolean accept(File file) {
            return true;
        }

        @Override
        public boolean accept(File file, String name) {
            return true;
        }
    };
    private AgentConfig agentConfig;

    public JarExecutor(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Master.Job.class, cmd -> onJob(cmd))
                .match(Master.FileJob.class, cmd -> onFileJob(cmd))
                .build();
    }

    private void onFileJob(Master.FileJob message) {
        Master.FileJob fileJob = message;
        try {
            if (fileJob.content != null) {
                FileUtils.touch(new File(agentConfig.getJob().getPath(), fileJob.uploadFileRequest.getFileName()));
                FileUtils.writeStringToFile(new File(agentConfig.getJob().getPath(), fileJob.uploadFileRequest.getFileName()), fileJob.content);
                getSender().tell(new Worker.FileUploadComplete(fileJob.uploadFileRequest, HostUtils.lookupIp()), getSelf());
            } else if (fileJob.remotePath != null) {
                FileUtils.touch(new File(agentConfig.getJob().getPath(), fileJob.uploadFileRequest.getFileName()));
                FileUtils.copyURLToFile(new URL(fileJob.remotePath), new File(agentConfig.getJob().getPath(), fileJob.uploadFileRequest.getFileName()));
                getSender().tell(new Worker.FileUploadComplete(fileJob.uploadFileRequest, HostUtils.lookupIp()), getSelf());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void onJob(final Master.Job job) {
        Cancellable abortLoop = getContext().system().scheduler().schedule(Duration.Zero(), Duration.create(60, TimeUnit.SECONDS),
                () -> {
                    runCancelJob(job);
                }, getContext().system().dispatcher());
        ActorRef sender = getSender();
        ExecutorService pool = Executors.newFixedThreadPool(1);
        ExecutionContextExecutorService ctx = ExecutionContexts.fromExecutorService(pool);
        Future<Object> f = future(() -> runJob(job), ctx);
        f.onSuccess(new OnSuccess<Object>() {
            @Override
            public void onSuccess(Object result) throws Throwable {
                log.info("Notify Worker job status {}", result);
                sender.tell(result, getSelf());
                abortLoop.cancel();
            }
        }, ctx);
        f.onFailure(new OnFailure() {
            @Override
            public void onFailure(Throwable throwable) throws Throwable {
                log.error(throwable.toString());
                abortLoop.cancel();
                sender.tell(new Worker.WorkFailed(null), getSelf());
                unhandled(job);
            }
        }, ctx);
    }

    private boolean getAbortStatus(String abortUrl, String trackingId) {
        log.info("Getting abort status: {}{}", abortUrl, trackingId);
        URL url = null;
        try {
            url = new URL(abortUrl + trackingId);
        } catch (MalformedURLException e) {
            log.error("Error on URL for receiving abort status: {}",e);
            e.printStackTrace();
        }
        try (InputStream input = url.openStream()) {
            String resultString = IOUtils.toString(input, StandardCharsets.UTF_8);
            return Boolean.parseBoolean(resultString);
        } catch (IOException e) {
            log.error("Error receiving abort status: {}",e);
            e.printStackTrace();
        }
        return false;
    }

    private void runCancelJob(Master.Job message) {
        if (getAbortStatus(message.abortUrl, message.trackingId)) {
            CommandLine cmdLine = new CommandLine("/bin/bash");
            cmdLine.addArgument(agentConfig.getJob().getJobArtifact("cancel"));
            cmdLine.addArgument(message.jobId);
            DefaultExecutor killExecutor = new DefaultExecutor();
            ExecuteWatchdog watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
            killExecutor.setWatchdog(watchdog);
            try {
                log.info("Cancel command: {}", cmdLine);
                killExecutor.execute(cmdLine);
                TaskEvent taskEvent = (TaskEvent) message.taskEvent;
                String outPath = agentConfig.getJob().getOutPath(taskEvent.getJobName(), message.jobId);
                String errPath = agentConfig.getJob().getErrorPath(taskEvent.getJobName(), message.jobId);
                Worker.Result result = new Worker.Result(-9, agentConfig.getUrl(errPath), agentConfig.getUrl(outPath), null, message);
                getSender().tell(new Worker.WorkFailed(result), getSelf());
            } catch (IOException e) {
                log.error(e, "Error cancelling job");
            }
        }
    }

    private Object runJob(Master.Job message) {
        Master.Job job = message;
        TaskEvent taskEvent = (TaskEvent) job.taskEvent;
        CommandLine cmdLine =  job.isJarSimulation ? getJarCommand(job, taskEvent): getScriptCommand(job, taskEvent);
        log.info("Verified Script worker received task: {}", message);

        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValues(agentConfig.getJob().getExitValues());
        ExecuteWatchdog watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
        executor.setWatchdog(watchdog);
        executor.setWorkingDirectory(new File(agentConfig.getJob().getPath()));
        FileOutputStream outFile = null;
        FileOutputStream errorFile = null;
        String outPath = "", errPath = "";
        try {
            outPath = agentConfig.getJob().getOutPath(taskEvent.getJobName(), job.jobId);
            errPath = agentConfig.getJob().getErrorPath(taskEvent.getJobName(), job.jobId);
            //create the std and err files
            outFile = FileUtils.openOutputStream(new File(outPath));
            errorFile = FileUtils.openOutputStream(new File(errPath));

            PumpStreamHandler psh = new PumpStreamHandler(new ExecLogHandler(outFile), new ExecLogHandler(errorFile));
            executor.setStreamHandler(psh);
            Map<String,String> envOptions = new HashMap<>();
            //additional user parameters
            if (taskEvent.getJobInfo().parameterString != null && !taskEvent.getJobInfo().parameterString.isEmpty()){
                envOptions.put("JAVA_OPTS" , taskEvent.getJobInfo().parameterString);
            }
            log.info("command: {} and env options {}", cmdLine,envOptions);
            int exitResult = executor.execute(cmdLine,envOptions);
            Worker.Result result = new Worker.Result(exitResult, agentConfig.getUrl(errPath), agentConfig.getUrl(outPath), null, job);
            log.info("Exit code: {}", exitResult);
            FileUtils.deleteQuietly(FileUtils.getFile(agentConfig.getJob().getJobDirectory(job.jobId, "")));
            if (executor.isFailure(exitResult) || exitResult == 1) {
                log.info("Jar Executor Failed, job: " + job.jobId);
                return new Worker.WorkFailed(result);
            } else {
                result = new Worker.Result(exitResult, agentConfig.getUrl(errPath), agentConfig.getUrl(outPath), agentConfig.getUrl(getMetricsPath(job)), job);
                log.info("Jar Executor Completed, job: " + result);
                return new Worker.WorkComplete(result);
            }

        } catch (Exception e) {
            log.error(e.toString());
            Worker.Result result = new Worker.Result(-1, agentConfig.getUrl(errPath), agentConfig.getUrl(outPath), null, job);
            log.info("Executor Encountered run time exception, result: " + result.toString());
            return new Worker.WorkFailed(result);
        } finally {
            IOUtils.closeQuietly(outFile);
            IOUtils.closeQuietly(errorFile);
        }
    }

    private CommandLine getScriptCommand(Master.Job job,TaskEvent taskEvent) {
        CommandLine cmdLine = new CommandLine(agentConfig.getJob().getCommand());
        Map<String, Object> map = new HashMap<>();

        map.put("path", new File(agentConfig.getJob().getJobArtifact(taskEvent.getJobName())));
        cmdLine.addArgument("${path}");
        //parameters come from the task event
        for (String pair : taskEvent.getParameters()) {
            cmdLine.addArgument(pair);
        }
        //download the simulation or jar file
        DownloadFile.downloadFile(job.jobFileUrl,agentConfig.getJob().getJobDirectory(job.jobId, SIMULATION, taskEvent.getJobInfo().getFileNameFromPackageName()));

        //job simulation artifact path
        cmdLine.addArgument("-sf").addArgument(agentConfig.getJob().getJobDirectory(job.jobId, SIMULATION));
        cmdLine.addArgument("-s").addArgument(taskEvent.getJobInfo().fileFullName);

        //download the data feed
        if(taskEvent.getJobInfo().hasDataFeed) {
            DownloadFile.downloadFile(job.dataFileUrl, agentConfig.getJob().getJobDirectory(job.jobId, DATA,taskEvent.getJobInfo().dataFileName));
            //job data feed  path
            cmdLine.addArgument("-df").addArgument(agentConfig.getJob().getJobDirectory(job.jobId,DATA));
        }
        
        //download the bodies feed
        if(taskEvent.getJobInfo().hasBodiesFeed) {
            DownloadFile.downloadFileAndUnzip(job.bodiesFileUrl, agentConfig.getJob().getJobDirectory(job.jobId, BODIES ,taskEvent.getJobInfo().bodiesFileName));
            //job data feed  path
            cmdLine.addArgument("-bdf").addArgument(agentConfig.getJob().getJobDirectory(job.jobId,BODIES));
        }
        
        //report file path
        cmdLine.addArgument("-rf").addArgument(agentConfig.getJob().getResultPath(job.roleId, job.jobId));
        cmdLine.addArgument("-nr").addArgument("-m");

        cmdLine.setSubstitutionMap(map);
        return cmdLine;
    }

    private CommandLine getJarCommand(Master.Job job, TaskEvent taskEvent) {
        CommandLine cmdLine = new CommandLine("java");
        cmdLine.addArgument("-jar");

        //download the data feed
        if(taskEvent.getJobInfo().hasDataFeed) {
            log.info("Downloading from {}  to {}", job.dataFileUrl,agentConfig.getJob().getJobDirectory(job.jobId, DATA,taskEvent.getJobInfo().dataFileName));
            DownloadFile.downloadFile(job.dataFileUrl, agentConfig.getJob().getJobDirectory(job.jobId, DATA,taskEvent.getJobInfo().dataFileName));
            //job data feed  path
        }
        cmdLine.addArgument("-DdataFolder=" + agentConfig.getJob().getJobDirectory(job.jobId,DATA));
        cmdLine.addArgument("-DresultsFolder=" + agentConfig.getJob().getResultPath(job.roleId, job.jobId));
        cmdLine.addArgument("-DnoReports=true");
        //parameters come from the task event
        for (String pair : taskEvent.getParameters()) {
            cmdLine.addArgument(pair);
        }
        log.info("Downloading jar to {} ",agentConfig.getJob().getJobDirectory(job.jobId, SIMULATION, taskEvent.getJobInfo().jarFileName));
        //download the simulation or jar file
        DownloadFile.downloadFile(job.jobFileUrl,agentConfig.getJob().getJobDirectory(job.jobId, SIMULATION, taskEvent.getJobInfo().jarFileName));//.jar

        cmdLine.addArgument(agentConfig.getJob().getJobDirectory(job.jobId, SIMULATION, taskEvent.getJobInfo().jarFileName));//.jar
        return cmdLine;
    }

    /**
     * Assumes there will only be one file in the directory
     */
    public String getMetricsPath(Master.Job job) {
        File dir = new File(agentConfig.getJob().getResultPath(job.roleId, job.jobId));
        log.info("Directory for metrics: {}", dir.getAbsolutePath());
        List<File> files = (List<File>) FileUtils.listFiles(dir, logFilter, logFilter);
        log.info("Files for metrics: {}", files);
        String result = files.stream().filter(f -> f.getName().endsWith(".log")).findFirst().get().getAbsolutePath();
        return result;
    }

    /**
     * Assumes there will only be one file in the directory
     */
    private String getMetrics(Master.Job job) {
        File dir = new File(agentConfig.getJob().getResultPath(job.roleId, job.jobId));
        log.info("Directory for metrics: {}", dir.getAbsolutePath());
        List<File> files = (List<File>) FileUtils.listFiles(dir, logFilter, logFilter);
        log.info("Files for metrics: {}", files);
        StringBuilder logMetrics = new StringBuilder();
        for (File file : files) {
            try {
                logMetrics.append(FileUtils.readFileToString(file));
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return logMetrics.toString();
    }

    class ExecLogHandler extends LogOutputStream {
        private FileOutputStream file;

        public ExecLogHandler(FileOutputStream file) {
            this.file = file;
        }

        @Override
        protected void processLine(String line, int level) {
            try {
                IOUtils.write(line, file);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }
}
