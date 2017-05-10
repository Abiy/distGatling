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

package com.walmart.gatling.repository;

import akka.actor.ActorRef;
import akka.util.Timeout;
import com.walmart.gatling.commons.AgentConfig;
import com.walmart.gatling.commons.JobSummary;
import com.walmart.gatling.commons.Master;
import com.walmart.gatling.commons.MasterClientActor;
import com.walmart.gatling.commons.ReportExecutor;
import com.walmart.gatling.commons.TaskEvent;
import com.walmart.gatling.commons.TrackingResult;
import com.walmart.gatling.endpoint.v1.SimulationJobModel;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.*;

/**
 * Created by walmart
 */
@Component
public class ServerRepository {

    private final Logger log = LoggerFactory.getLogger(ServerRepository.class);

    private ActorRef router;
    private AgentConfig agentConfig;

    @Autowired
    public ServerRepository(ActorRef router, AgentConfig agentConfig) {
        this.router = router;
        this.agentConfig = agentConfig;
    }

    /**
     * Sends the message to the master and waits for response using ask pattern
     *
     * @param message
     * @param timeoutInSeconds
     * @return
     */
    private Object sendToMaster(Object message, int timeoutInSeconds) {
        Timeout timeout = new Timeout(timeoutInSeconds, TimeUnit.SECONDS);
        Future<Object> future = ask(router, message, timeout);
        try {
            Object info = Await.result(future, timeout.duration());
            if (info instanceof MasterClientActor.Ok) {
                MasterClientActor.Ok ok = (MasterClientActor.Ok) info;
                log.debug("Ok Message from server just got here: {}", info);
                return ok.getMsg();
            }
        } catch (Exception e) {
            log.error("Error fetching status from server {}", e);
        }
        return null;
    }

    /**
     * Retrieves the cluster status from the master
     * TODO: create a separate immutable class to represent the request and the response
     *
     * @param message
     * @return
     */
    public Master.ServerInfo getServerStatus(Master.ServerInfo message) {
        Object result = sendToMaster(message, 6);
        if (result != null && result instanceof Master.ServerInfo) {
            Master.ServerInfo info = (Master.ServerInfo) result;
            return info;
        }
        return new Master.ServerInfo();
    }

    /**
     * Generates a unique tracking and submit the job to the master via the master proxy
     * if the job is properly submitted return the tracking identifier
     *
     * @param simulationJobModel
     * @return
     * @throws Exception
     */
    public Optional<String> submitSimulationJob(SimulationJobModel simulationJobModel) throws Exception {
        String trackingId = UUID.randomUUID().toString();
        List<Pair<String, String>> parameters = Arrays.asList(new Pair<>("0", "-nr"), new Pair<>("1", "-m"),
                new Pair<>("2", "-s"), new Pair<>("3", simulationJobModel.getFileFullName()));
        JobSummary.JobInfo jobinfo = JobSummary.JobInfo.newBuilder()
                .withCount(simulationJobModel.getCount())
                .withJobName("gatling")
                .withPartitionAccessKey(simulationJobModel.getPartitionAccessKey())
                .withPartitionName(simulationJobModel.getRoleId())
                .withUser(simulationJobModel.getUser())
                .withTrackingId(trackingId)
                .withFileFullName(simulationJobModel.getFileFullName())
                .build();
        //cmdLine.addArgument("-rf").addArgument(agentConfig.getJob().getResultPath(job.roleId,job.jobId));
        Timeout timeout = new Timeout(6, TimeUnit.SECONDS);
        int success = 0;
        for (int i = 0; i < simulationJobModel.getCount(); i++) {
            TaskEvent taskEvent = new TaskEvent();
            taskEvent.setJobName("gatling"); //the gatling.sh script is the gateway for simulation files
            taskEvent.setJobInfo(jobinfo);
            taskEvent.setParameters(parameters);
            Future<Object> future = ask(router, new Master.Job(simulationJobModel.getRoleId(), taskEvent, trackingId,
                    agentConfig.getAbortUrl(),
                    agentConfig.getJobFileUrl(simulationJobModel.getSimulation())),
                    timeout);
            Object result = Await.result(future, timeout.duration());
            if (result instanceof MasterClientActor.Ok) {
                success++;
                log.debug("Ok message from server just got here, this indicates the job was successfully posted to the master: {}", result);
            }
        }

        if (success == simulationJobModel.getCount()) {
            log.debug("Job Successfully submitted to master");
            return Optional.of(trackingId);
        }
        return Optional.empty();
    }

    public TrackingResult getTrackingInfo(String trackingId) {
        Object result = sendToMaster(new Master.TrackingInfo(trackingId), 60);
        if (result != null && result instanceof TrackingResult) {
            TrackingResult info = (TrackingResult) result;
            return info;
        }
        return new TrackingResult(0, 0);
    }

    public boolean abortJob(String trackingId) {
        Object result = sendToMaster(new Master.TrackingInfo(trackingId, true), 60);
        if (result != null && result instanceof TrackingResult) {
            TrackingResult info = (TrackingResult) result;
            return info.isCancelled();
        }
        return false;
    }

    public Optional<ReportExecutor.ReportResult> generateReport(String trackingId) {

        TaskEvent taskEvent = new TaskEvent();
        taskEvent.setJobName("gatling");
        taskEvent.setParameters(Arrays.asList(
                new Pair<>("0", "-ro")
        ));
        Object result = sendToMaster(new Master.Report(trackingId, taskEvent), 180);

        log.info("Report generated {}", result);
        if (result != null && result instanceof ReportExecutor.ReportResult) {
            log.info("Report generated accurately {}", result);
            return Optional.of((ReportExecutor.ReportResult) result);
        }
        return Optional.empty();
    }

    /**
     * Given the path from the staging area the master instructs workers to pull new files supplied by users
     *
     * @param path
     * @param name
     * @param role
     * @param type
     * @return
     */
    public Optional<String> uploadFile(String path, String name, String role, String type) {
        String trackingId = UUID.randomUUID().toString();
        Master.UploadFile uploadFileRequest = new Master.UploadFile(trackingId, path, name, role, type);
        Object result = sendToMaster(uploadFileRequest, 5);
        log.info("UploadFile request sent {}", result);
        if (result != null && result instanceof Master.Ack) {
            return Optional.of(((Master.Ack) result).getWorkId());
        }
        return Optional.empty();
    }

    /**
     * Tracks file upload status and indicates which workers have successfully pulled the new files
     *
     * @param uploadInfo
     * @return
     */
    public Optional<Master.UploadInfo> getUploadStatus(Master.UploadInfo uploadInfo) {
        Object result = sendToMaster(uploadInfo, 5);
        if (result != null && result instanceof Master.UploadInfo) {
            return Optional.of((Master.UploadInfo) result);
        }
        return Optional.empty();
    }


    public List<JobSummary> getJobSummary() {
        Object result = sendToMaster(new Master.JobSummaryInfo(), 60);

        List<JobSummary> summaries = (List<JobSummary>) result;
        return summaries;

    }
}
