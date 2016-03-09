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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import akka.event.Logging;
import akka.event.LoggingAdapter;
import javafx.util.Pair;

/**
 * Created by ahailemichael on 8/17/15.
 */
public class ScriptExecutor extends WorkExecutor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private AgentConfig agentConfig;

    public ScriptExecutor(AgentConfig agentConfig){

        this.agentConfig = agentConfig;
    }

    @Override
    public void onReceive(Object message) {
        log.info("Script worker received task: {}", message);
        if (message instanceof Master.Job) {
            runJob(message);
        }
        else if (message instanceof Master.FileJob) {
            Master.FileJob fileJob = (Master.FileJob) message;
            try {
                FileUtils.touch(new File(agentConfig.getJob().getPath(),fileJob.uploadFileRequest.getFileName()));
                FileUtils.writeStringToFile(new File(agentConfig.getJob().getPath(),fileJob.uploadFileRequest.getFileName()),fileJob.content);
                getSender().tell(new Worker.FileUploadComplete(fileJob.uploadFileRequest,HostUtils.lookupIp()), getSelf());
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    private void runJob(Object message) {
        Master.Job job = (Master.Job) message;
        TaskEvent taskEvent = (TaskEvent)job.taskEvent;
        if (true) {
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
            cmdLine.addArgument("-rf").addArgument(agentConfig.getJob().getResultPath(job.roleId,job.jobId));

            cmdLine.setSubstitutionMap(map);
            DefaultExecutor executor = new DefaultExecutor();
            executor.setExitValues(agentConfig.getJob().getExitValues());
            ExecuteWatchdog watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
            executor.setWatchdog(watchdog);
            executor.setWorkingDirectory(new File(agentConfig.getJob().getPath()));
            FileOutputStream outFile = null;
            FileOutputStream errorFile = null;
            try {
                String outPath = agentConfig.getJob().getOutPath(taskEvent.getJobName(), job.jobId);
                String errPath = agentConfig.getJob().getErrorPath(taskEvent.getJobName(), job.jobId);
                //create the std and err files
                outFile = FileUtils.openOutputStream(new File(outPath));
                errorFile = FileUtils.openOutputStream(new File(errPath));

                PumpStreamHandler psh = new PumpStreamHandler(new ExecLogHandler(outFile),new ExecLogHandler(errorFile));
                executor.setStreamHandler(psh);
                log.info("command: {}",cmdLine);
                int exitResult = executor.execute(cmdLine);
                Worker.Result result = new Worker.Result(exitResult,agentConfig.getUrl(errPath),agentConfig.getUrl(outPath),  null, job);
                if(executor.isFailure(exitResult)){
                    log.info("Script Executor Failed, result: " +result.toString());
                    getSender().tell(new Worker.WorkFailed(result), getSelf());
                    return;
                }
                else{
                    result = new Worker.Result(exitResult,agentConfig.getUrl(errPath),agentConfig.getUrl(outPath),getMetrics(job), job);
                    log.info("Script Executor Completed, result: " +result.toString());
                    getSender().tell(new Worker.WorkComplete(result), getSelf());
                    return;
                }

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
           finally {
                IOUtils.closeQuietly(outFile);
                IOUtils.closeQuietly(errorFile);
            }

        }
        else{
            unhandled(message);
        }
    }

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
        private  FileOutputStream file;

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
}
