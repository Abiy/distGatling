package com.walmart.gatling.commons;

/**
 * Created by walmart.
 */

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import akka.event.Logging;
import akka.event.LoggingAdapter;
import javafx.util.Pair;

/**
 * Created by ahailemichael on 8/17/15.
 */
public class ReportExecutor extends WorkExecutor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private AgentConfig agentConfig;

    public ReportExecutor(AgentConfig agentConfig){

        this.agentConfig = agentConfig;
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof Master.GenerateReport) {
            Master.GenerateReport job = (Master.GenerateReport) message;
            runJob(job);
        }
        else {
            unhandled(message);
        }
    }

    private void runJob(Master.GenerateReport job) {
        TaskEvent taskEvent = job.reportJob.taskEvent;
        CommandLine cmdLine = new CommandLine(agentConfig.getJob().getCommand());
        Map<String, Object> map = new HashMap<>();

        map.put("path", new File(agentConfig.getJob().getJobArtifact(taskEvent.getJobName())));
        cmdLine.addArgument("${path}");

        //parameters come from the task event
        for (Pair<String, String> pair : taskEvent.getParameters()) {
            cmdLine.addArgument(pair.getValue());
        }
        String dir = agentConfig.getJob().getLogDirectory()+ "reports/" + job.reportJob.trackingId + "/";
        cmdLine.addArgument(dir);

        cmdLine.setSubstitutionMap(map);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValues(agentConfig.getJob().getExitValues());
        ExecuteWatchdog watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
        executor.setWatchdog(watchdog);
        executor.setWorkingDirectory(new File(agentConfig.getJob().getPath()));
        FileOutputStream outFile = null;
        FileOutputStream errorFile = null;
        try {
            List<String> resultFiles = new ArrayList<>(job.results.size());
            //download all files adn
            /*int i=0;
            for (Worker.Result result : job.results) {
                String destFile = dir  + i++ + ".log";
                resultFiles.add(destFile);
                DownloadFile.downloadFile(result.metrics,destFile);
            }*/
            AtomicInteger index = new AtomicInteger();
            job.results.parallelStream().forEach(result -> {
                String destFile = dir  + index.incrementAndGet() + ".log";
                resultFiles.add(destFile);
                DownloadFile.downloadFile(result.metrics,destFile);
            });
            String outPath = agentConfig.getJob().getOutPath(taskEvent.getJobName(), job.reportJob.trackingId);
            String errPath = agentConfig.getJob().getErrorPath(taskEvent.getJobName(), job.reportJob.trackingId);
            //create the std and err files
            outFile = FileUtils.openOutputStream(new File(outPath));
            errorFile = FileUtils.openOutputStream(new File(errPath));

            PumpStreamHandler psh = new PumpStreamHandler(new ExecLogHandler(outFile),new ExecLogHandler(errorFile));

            executor.setStreamHandler(psh);
            System.out.println(cmdLine);
            int exitResult = executor.execute(cmdLine);
            ReportResult result ;
            if(executor.isFailure(exitResult)){
                result = new ReportResult(dir,job.reportJob, false);
                log.info("Report Executor Failed, result: " +job.toString());
            }
            else{
                result = new ReportResult(job.reportJob.getHtml() ,job.reportJob, true);
                log.info("Report Executor Completed, result: " +result.toString());
            }
            for (String resultFile : resultFiles) {
                FileUtils.deleteQuietly(new File(resultFile));
            }
            getSender().tell(result, getSelf());

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }finally {
            IOUtils.closeQuietly(outFile);
            IOUtils.closeQuietly(errorFile);
        }

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

    public static final class ReportResult implements Serializable {
        public final Object result;
        public final Master.Report  report;
        public final boolean Sucess ;
        public ReportResult(Object result, Master.Report report, boolean sucess) {
            this.result = result;
            this.report = report;
            Sucess = sucess;
        }

        @Override
        public String toString() {
            return "ReportResult{" +
                    "result=" + result +
                    ", report=" + report +
                    ", Sucess=" + Sucess +
                    '}';
        }
    }
}
