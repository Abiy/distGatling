package com.walmart.gatling.commons;

/**
 * Created by ahailemichael on 8/20/15.
 */

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;

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

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.dispatch.ExecutionContexts;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import javafx.util.Pair;
import scala.concurrent.ExecutionContextExecutorService;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import static akka.dispatch.Futures.future;

/**
 * Created by ahailemichael on 8/17/15.
 */
public class ScriptExecutor extends WorkExecutor {

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
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private AgentConfig agentConfig;

    public ScriptExecutor(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;
    }

    @Override
    public void onReceive(Object message) {
        //log.info("Script worker received task: {}", message);
        if (message instanceof Master.Job) {
            Cancellable abortLoop = getContext().system().scheduler().schedule(Duration.Zero(), Duration.create(60, TimeUnit.SECONDS),
                    () -> {
                        Master.Job job = (Master.Job) message;
                        runCancelJob(job);
                    }, getContext().system().dispatcher());
            ActorRef sender = getSender();
            ExecutorService pool = Executors.newFixedThreadPool(1);
            ExecutionContextExecutorService ctx = ExecutionContexts.fromExecutorService(pool);
            Future<Object> f = future(() -> runJob(message), ctx);
            f.onSuccess(new OnSuccess<Object>() {
                @Override
                public void onSuccess(Object result) throws Throwable {
                    log.info("notify Worker job status {}",result);
                    sender.tell(result, getSelf());
                    abortLoop.cancel();
                }
            }, ctx);
            f.onFailure(new OnFailure() {
                @Override
                public void onFailure(Throwable throwable) throws Throwable {
                    log.error(throwable.toString());
                    abortLoop.cancel();
                    unhandled(message);
                }
            }, ctx);
            //getSender().tell(runJob(message));
        } else if (message instanceof Master.FileJob) {
            Master.FileJob fileJob = (Master.FileJob) message;
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
    }

    private boolean getAbortStatus(String abortUrl, String trackingId) {
        log.info("Getting abort status: {}{}" , abortUrl,trackingId );
        URL url = null;
        try {
            url = new URL(abortUrl  + trackingId);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try (InputStream input = url.openStream()) {
            String resultString = IOUtils.toString(input, StandardCharsets.UTF_8);
            return Boolean.parseBoolean(resultString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void runCancelJob(Master.Job message) {
       if(getAbortStatus(message.abortUrl,message.trackingId)) {
           CommandLine cmdLine = new CommandLine("/bin/bash");
           cmdLine.addArgument(agentConfig.getJob().getJobArtifact("cancel"));
           cmdLine.addArgument(message.jobId);
           DefaultExecutor killExecutor = new DefaultExecutor();
           ExecuteWatchdog watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
           killExecutor.setWatchdog(watchdog);
           try {
               log.info("Cancel command: {}", cmdLine);
               killExecutor.execute(cmdLine);
           } catch (IOException e) {
               log.error(e,"Error cancelling job");
           }
       }
    }

    private Object runJob(Object message) {
        Master.Job job = (Master.Job) message;
        TaskEvent taskEvent = (TaskEvent) job.taskEvent;

        CommandLine cmdLine = new CommandLine(agentConfig.getJob().getCommand());
        log.info("Verified Script worker received task: {}", message);
        Map<String, Object> map = new HashMap<>();

        if (StringUtils.isNotEmpty(agentConfig.getJob().getMainClass()))
            cmdLine.addArgument(agentConfig.getJob().getCpOrJar());

        map.put("path", new File(agentConfig.getJob().getJobArtifact(taskEvent.getJobName())));
        cmdLine.addArgument("${path}");

        if (!StringUtils.isEmpty(agentConfig.getJob().getMainClass())) {
            cmdLine.addArgument(agentConfig.getJob().getMainClass());
        }
        //parameters come from the task event
        for (Pair<String, String> pair : taskEvent.getParameters()) {
            cmdLine.addArgument(pair.getValue());
        }
        cmdLine.addArgument("-rf").addArgument(agentConfig.getJob().getResultPath(job.roleId, job.jobId));

        cmdLine.setSubstitutionMap(map);
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
            log.info("command: {}", cmdLine);
            int exitResult = executor.execute(cmdLine);
            //executor.getWatchdog().destroyProcess().
            Worker.Result result = new Worker.Result(exitResult, agentConfig.getUrl(errPath), agentConfig.getUrl(outPath), null, job);
            log.info("Exit code: {}", exitResult);
            if (executor.isFailure(exitResult) || exitResult == 1) {
                log.info("Script Executor Failed, job: " + job.jobId);
                //getSender().tell(new Worker.WorkFailed(result), getSelf());
                return new Worker.WorkFailed(result);
            } else {
                result = new Worker.Result(exitResult, agentConfig.getUrl(errPath), agentConfig.getUrl(outPath), agentConfig.getUrl(getMetricsPath(job)), job);
                log.info("Script Executor Completed, job: " + result);
                //getSender().tell(new Worker.WorkComplete(result), getSelf());
                return new Worker.WorkComplete(result);
            }

        } catch (IOException e) {
            log.error(e.toString());
            Worker.Result result = new Worker.Result(-1, agentConfig.getUrl(errPath), agentConfig.getUrl(outPath), null, job);
            log.info("Executor Encountered run time exception, result: " + result.toString());
            //getSender().tell(new Worker.WorkFailed(result), getSelf());
            return new Worker.WorkFailed(result);
        } finally {
            IOUtils.closeQuietly(outFile);
            IOUtils.closeQuietly(errorFile);
        }
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
    public String getMetrics(Master.Job job) {
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
