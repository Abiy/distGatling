/*
 *
 *   Copyright 2016 alh Technology
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

package com.alh.gatling.service;

import static akka.pattern.Patterns.ask;

import akka.actor.ActorRef;
import akka.util.Timeout;
import com.alh.gatling.commons.AgentConfig;
import com.alh.gatling.commons.JobSummary;
import com.alh.gatling.commons.MasterClientActor;
import com.alh.gatling.commons.ReportExecutor;
import com.alh.gatling.commons.TaskEvent;
import com.alh.gatling.commons.TrackingResult;
import com.alh.gatling.domain.SimulationJobModel;
import com.alh.gatling.service.exception.UnknownResourceException;
import com.alh.gatling.commons.KubernetesMaster;
import com.alh.gatling.commons.Master;
import com.alh.gatling.commons.KubernetesService;
import com.alh.gatling.commons.exception.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import scala.concurrent.Await;
import scala.concurrent.Future;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Component
public class ServerRepository {

    private final Logger log = LoggerFactory.getLogger(ServerRepository.class);

    private ActorRef router;
    private AgentConfig agentConfig;
    private boolean isRunningOnKubernetes;

    @Autowired
    public ServerRepository(ActorRef router, AgentConfig agentConfig, @Value("${kubernetes.kubernetes}") String isRunningOnKubernetes) {
        this.router = router;
        this.agentConfig = agentConfig;
        this.isRunningOnKubernetes = Boolean.parseBoolean(isRunningOnKubernetes);
    }


    /**
     * Sends the message to the master and waits for response using ask pattern
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
     * Retrieves the cluster status from the master TODO: create a separate immutable class to represent the request and
     * the response
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
     * Generates a unique tracking and submit the job to the master via the master proxy if the job is properly
     * submitted return the tracking identifier
     */
    public Optional<String> submitSimulationJob(SimulationJobModel simulationJobModel) throws Exception {
        String trackingId = UUID.randomUUID().toString();
        List<String> parameters = Arrays.asList();//"-nr",  "-m", "-s",  simulationJobModel.getFileFullName());

        boolean hasResourcesFeed = !(simulationJobModel.getResourcesFile() == null || simulationJobModel
            .getResourcesFile().isEmpty());
        JobSummary.JobInfo jobinfo = JobSummary.JobInfo.newBuilder()
            .withCount(simulationJobModel.getCount())
            .withJobName("gatling")
            .withPartitionAccessKey(simulationJobModel.getPartitionAccessKey())
            .withPartitionName(simulationJobModel.getRoleId())
            .withUser(simulationJobModel.getUser())
            .withTrackingId(trackingId)
            .withHasResourcesFeed(hasResourcesFeed)
            .withParameterString(simulationJobModel.getParameterString())
            .withFileFullName(simulationJobModel.getFileFullName())
            .withResourcesFileName(getResourcesFileName(simulationJobModel, hasResourcesFeed))
            .build();
        Timeout timeout = new Timeout(6, TimeUnit.SECONDS);
        int success = 0;

        if(this.isRunningOnKubernetes) {
            KubernetesService kubernetesService = new KubernetesService(simulationJobModel.getKubernetesNamespace());
            Future<Object> future1 = ask(router, new Master.SubmitNewSimulation(trackingId, kubernetesService),
                                         timeout);
            Object res = Await.result(future1, timeout.duration());
            if (res instanceof MasterClientActor.Ok) {
                log.debug("Successfully start submitting a new simulation with trackingID={}", trackingId);
            }
        }
        for (int i = 0; i < simulationJobModel.getCount(); i++) {
            TaskEvent taskEvent = new TaskEvent();
            taskEvent.setJobName("gatling"); //the gatling.sh script is the gateway for simulation files
            taskEvent.setJobInfo(jobinfo);
            taskEvent.setParameters(new ArrayList<>(parameters));

            Future<Object> future = ask(router, new Master.Job(simulationJobModel.getRoleId(), taskEvent, trackingId,
                                                               agentConfig.getAbortUrl(),
                                                               agentConfig
                                                                   .getJobFileUrl(simulationJobModel.getSimulation()),
                                                               agentConfig.getJobFileUrl(
                                                                   simulationJobModel.getResourcesFile()),
                                                               false,
                                                               simulationJobModel.getCount()),
                                        timeout);
            Object result = Await.result(future, timeout.duration());
            if (result instanceof MasterClientActor.Ok) {
                success++;
                log.debug(
                    "Ok message from server just got here, this indicates the job was successfully posted to the master: {}",
                    result);
            }
        }

        if (success == simulationJobModel.getCount()) {
            log.debug("Job with trackingId={}, successfully submitted to master", trackingId);
            return Optional.of(trackingId);
        }
        return Optional.empty();
    }

    private String getResourcesFileName(SimulationJobModel simulationJobModel, boolean hasBodiesFeed) {
        if (!hasBodiesFeed) {
            return "";
        }
        String[] splits = simulationJobModel.getResourcesFile().split("/");
        if (splits.length > 0) //return the last name
        {
            return splits[splits.length - 1];
        }
        return "";
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
        taskEvent.setParameters(new ArrayList<>(Arrays.asList("-ro")));
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

    public String getLogResult(String trackingId, String taskJobId, String logType)
        throws NotFoundException, UnknownResourceException {
        List<JobSummary> summaries = getJobSummary();
        Optional<JobSummary> summary = summaries.stream()
            .filter(p -> p.getJobInfo().trackingId.equalsIgnoreCase(trackingId)).findFirst();
        log.info("Processing  get job detail.");

        if (summary.isPresent()) {
            Optional<TaskEvent> taskEvent = summary.get().getTaskInfoList().stream()
                .filter(p -> p.getTaskJobId().equalsIgnoreCase(taskJobId)).findFirst();
            if (taskEvent.isPresent()) {

                Object result = sendToMaster(new KubernetesMaster.RunningOnKubernetes(), 180);

                if (((KubernetesMaster.RunningOnKubernetes) result).isRunningOnKubernetes) {
                    Object logsResult = sendToMaster(new Master.JobLogs(trackingId, taskJobId), 180);

                    String log = ((Master.JobLogs) logsResult).getWorkerLog().errorLog;
                    if ("std".equalsIgnoreCase(logType)) {
                        log = ((Master.JobLogs) logsResult).getWorkerLog().stdLog;
                    }
                    return log;
                } else {
                    String logPath = taskEvent.get().getErrorLogPath();
                    if ("std".equalsIgnoreCase(logType)) {
                        logPath = taskEvent.get().getStdLogPath();
                    }
                    return Master.loadLogFromPath(logPath);
                }


            } else {
                throw new UnknownResourceException("The Specified taskJobId id is not available.");
            }
        } else {
            throw new UnknownResourceException("The Specified tracking id id is not available.");
        }
    }
}
