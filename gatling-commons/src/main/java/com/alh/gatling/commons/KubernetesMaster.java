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

package com.alh.gatling.commons;

import com.alh.gatling.commons.exception.NotFoundException;

import io.kubernetes.client.models.V1Status;

import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.client.ClusterClientReceptionist;
import akka.persistence.Recovery;
import jersey.repackaged.com.google.common.cache.CacheBuilder;
import jersey.repackaged.com.google.common.cache.CacheLoader;
import jersey.repackaged.com.google.common.cache.LoadingCache;
import jersey.repackaged.com.google.common.collect.ImmutableList;
import scala.collection.JavaConversions;
import scala.concurrent.duration.FiniteDuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Master component for running simulation in a Kubernetes environment
 */
public class KubernetesMaster extends Master {

//    private KubernetesService kubernetesService;

    // connected workers grouped per simulation id
    private LoadingCache<String, List<WorkerState>> workersPerTrackingIdCache = CacheBuilder.newBuilder()
        .build(new CacheLoader<String, List<WorkerState>>() {
            @Override
            public List<WorkerState> load(final String s) throws Exception {
                return null;
            }
        });

    // reports stored per simulation id after all work for a simulation is done
    private LoadingCache<String, ReportExecutor.ReportResult> reportPerTrackingIdCache = CacheBuilder.newBuilder()
        .build(new CacheLoader<String, ReportExecutor.ReportResult>() {
            @Override
            public ReportExecutor.ReportResult load(final String s) throws Exception {
                return null;
            }
        });

    // logs stored per job and simulation before a worker gets destroyed
    private LoadingCache<String, HashMap<String, GenericMaster.WorkerLog>> logsPerSimulation = CacheBuilder.newBuilder()
        .build(new CacheLoader<String, HashMap<String, Master.WorkerLog>>() {
            @Override
            public HashMap<String, Master.WorkerLog> load(final String s) throws Exception {
                return null;
            }
        });

    // KubernetesService clients per simulation id
    private LoadingCache<String, KubernetesService> kubernetesClientsPerSimulation = CacheBuilder.newBuilder()
        .build(new CacheLoader<String, KubernetesService>() {
            @Override
            public KubernetesService load(final String s) throws Exception {
                return null;
            }
        });

    // job names per simulation
    private Map<String, List<String>> jobsPerTrackingID = new ConcurrentHashMap<>();


    public KubernetesMaster(FiniteDuration workTimeout, AgentConfig agentConfig) {
        this.workTimeout = workTimeout;
        this.agentConfig = agentConfig;
        this.reportExecutor = getContext()
            .watch(getContext().actorOf(Props.create(ReportExecutor.class, agentConfig), "report"));
        ClusterClientReceptionist.get(getContext().system()).registerService(getSelf());
        this.cleanupTask = getContext().system().scheduler()
            .schedule(workTimeout.div(2), workTimeout.div(2), getSelf(), CleanupTick, getContext().dispatcher(),
                      getSelf());
    }

    public static Props props(FiniteDuration workTimeout, AgentConfig agentConfig) {
        return Props.create(KubernetesMaster.class, workTimeout, agentConfig);
    }

    @Override
    public void postStop() {
        cleanupTask.cancel();
    }

    /**
     * Giving work to a specific group of workers
     *
     * @param jobList list of jobs which have to be started
     */
    private void notifySpecificWorkers(List<String> jobList) {
        for (String job : jobList) {
            String workerId = "gatling-worker." + job;
            WorkerState workerState = workers.get(workerId);

            if (workerState.status.isIdle()) {
                workerState.ref.tell(MasterWorkerProtocol.WorkIsReady.getInstance(), getSelf());
            }
        }
    }


    @Override
    public String persistenceId() {
        for (String role : JavaConversions.asJavaIterable((Cluster.get(getContext().system()).selfRoles()))) {
            if (role.startsWith("backend-")) {
                return role + "-master";
            }
        }
        return "kubernetes-master";

    }

    @Override
    public Recovery recovery() {
        return Recovery.create(100);
    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
            .match(JobDomainEvent.class, p -> {
                jobDatabase = jobDatabase.updated(p);
                log.info("Replayed {}", p.getClass().getSimpleName());
            })
            .match(UploadFile.class, p -> {
                log.info("Replayed {}", p.getClass().getSimpleName());
            })
            .build();
    }

    @Override
    public Receive createReceive() {
        return createReceiveBuilder()
            .match(ReportExecutor.ReportResult.class, cmd -> storeReportResult(cmd))
            .match(Master.JobLogs.class, cmd -> returnLogs(cmd))
            .match(Master.SubmitNewSimulation.class, cmd -> startSubmittingSimulation(cmd))
            .matchEquals(CleanupTick, cmd -> onCleanupTick())
            .matchAny(cmd -> unhandled(cmd))
            .build();
    }


    public void onCheckRunningOnKubernetes() {
        getSender().tell(new KubernetesMaster.RunningOnKubernetes(true), getSelf());
    }

    private void startSubmittingSimulation(Master.SubmitNewSimulation cmd){
        //set a KubernetesService client for current trackingId
        kubernetesClientsPerSimulation.put(cmd.trackingId,cmd.kubernetesService);
        getSender().tell(new Ack(cmd.trackingId), getSelf());
    }

    /**
     * Return logs stored for done worker All the logs are stored when workers finish their work
     *
     * @param cmd information about the worker logs are needed from
     */
    private void returnLogs(Master.JobLogs cmd) {
        try {
            getSender()
                .tell(new JobLogs(cmd.trackingId, cmd.jobId, logsPerSimulation.get(cmd.trackingId).get(cmd.jobId)),
                      getSelf());
        } catch (ExecutionException e) {
            log.warning("Can't get log for workId={}", cmd.jobId);
        }


    }

    /**
     * When work is done for a simulation, results are stored locally before the workers are destroyed
     *
     * @param resultReport results for all the work done for a simulation
     */
    private void storeReportResult(ReportExecutor.ReportResult resultReport) {

        String trackingId = resultReport.report.trackingId;
        try {
            // results are cached
            this.reportPerTrackingIdCache.put(trackingId, resultReport);

            //save logs for this simulation workers
            storeLogsForSimulation(trackingId);

            // disconnect workers from master through akka
            for (WorkerState workerState : workersPerTrackingIdCache.get(trackingId)) {
                getContext().stop(workerState.ref);
            }
        } catch (ExecutionException e) {
            log.error("Error while stopping workers");
        } finally {
            // delete kubernetes pods for workers related to this simulation
            for (String job : jobsPerTrackingID.get(trackingId)) {
                try {
                    KubernetesService kubernetesService = kubernetesClientsPerSimulation.get(trackingId);
                    V1Status status = kubernetesService.deleteDeployment(job);
                    if (status.getStatus().equals("Success")) {
                        log.info("Destroyed pod for workId={}", job);
                        workers.remove(("gatling-worker." + job));
                    } else {
                        log.error("Unable to delete deployment for workId={}", job);
                    }
                } catch (ExecutionException e) {
                    log.error("Can't get kubernetes client for deleting pod for trackingId={}", trackingId);
                }

            }

            // remove destroyed workers and done simulation
            kubernetesClientsPerSimulation.asMap().remove(trackingId);
            workersPerTrackingIdCache.asMap().remove(trackingId);
            jobsPerTrackingID.remove(trackingId);
        }
    }

    /**
     * Store logs for a simulation before the workers for that simulation are destroyed
     *
     * @param trackingId id of the simulation we need logs
     */
    private void storeLogsForSimulation(String trackingId) {

        if (!logsPerSimulation.asMap().containsKey(trackingId)) {
            logsPerSimulation.put(trackingId, new HashMap<>());
        }
        for (TaskEvent taskEvent : jobDatabase.getJobSummary().get(trackingId).getTaskInfoList()) {
            try {
                String errPath = loadLogFromPath(taskEvent.getErrorLogPath());
                String stdPath = loadLogFromPath(taskEvent.getStdLogPath());
                logsPerSimulation.get(trackingId)
                    .put(taskEvent.getTaskJobId(), new GenericMaster.WorkerLog(errPath, stdPath));
            } catch (NotFoundException e) {
                log.warning("Can't get logs for workId={}", taskEvent.getTaskJobId());
            } catch (ExecutionException e) {
                log.warning("Logs haven't been cached for workId={}", taskEvent.getTaskJobId());
            }
        }
    }

    /**
     * store logs for a specific worker after it finishes it's job
     *
     * @param result result from a worker
     */
    private void storeLogsForJob(Worker.Result result) {
        if (!logsPerSimulation.asMap().containsKey(result.job.trackingId)) {
            logsPerSimulation.put(result.job.trackingId, new HashMap<>());
        }
        try {
            String errPath = loadLogFromPath(result.errPath);
            String stdPath = loadLogFromPath(result.stdPath);
            logsPerSimulation.get(result.job.trackingId)
                .put(result.job.jobId, new GenericMaster.WorkerLog(errPath, stdPath));
        } catch (NotFoundException e) {
            log.warning("Can't get logs for workId={}", result.job.jobId);
        } catch (ExecutionException e) {
            log.warning("Logs haven't been cached for workId={}", result.job.jobId);
        }
    }

    protected void onJobSummary() {
        getSender().tell(ImmutableList.copyOf(jobDatabase.getJobSummary().values()), getSelf());
    }

    protected void onJob(Job cmd) {
        final String workId = cmd.jobId;
        // idempotent
        if (jobDatabase.isAccepted(workId)) {
            getSender().tell(new Ack(workId), getSelf());
        } else {
            log.info("Accepted workId={}", workId);
            persist(new JobState.JobAccepted(cmd), event -> {
                // Ack back to original sender for accepting the job
                getSender().tell(new Ack(event.job.jobId), getSelf());
                jobDatabase = jobDatabase.updated(event);

                // create a kubernetes pod for running this job
                KubernetesService kubernetesService = kubernetesClientsPerSimulation.get(cmd.trackingId);
                String deploymentName = kubernetesService.createDeploy(cmd.jobId);
                kubernetesService.waitUntilDeploymentIsReady(deploymentName, 1);
                log.info("Kubernetes pod is up for workId={}", cmd.jobId);

                //keep track of jobs per simulation
                if (jobsPerTrackingID.containsKey(cmd.trackingId)) {
                    jobsPerTrackingID.get(cmd.trackingId).add(cmd.jobId);
                } else {
                    jobsPerTrackingID.put(cmd.trackingId, new ArrayList<String>());
                    jobsPerTrackingID.get(cmd.trackingId).add(cmd.jobId);
                }

                // check if all work for a simulation is registered
                if (jobsPerTrackingID.get(cmd.trackingId).size() == cmd.expectedWorkers) {
                    log.info("All the work for trackingId={} is registered", cmd.trackingId);
                }

            });
        }
    }

    protected void onReport(Object cmd) {
        String trackingId = ((Report) cmd).trackingId;
        log.info("Accepted report request for trackingId={}: {}", trackingId, cmd);

        try {
            // give the report stored for a specific simulation
            getSender().tell(reportPerTrackingIdCache.get(trackingId), getSelf());

        } catch (ExecutionException e) {
            log.warning("Can't send the report from master: {}", e);
        }
    }

    protected void onServerInfo(Object cmd) {
        log.info("Accepted Server info request: {}", cmd);
        getSender().tell(new ServerInfo(workers), getSelf());
    }

    protected void onWorkFailed(MasterWorkerProtocol.WorkFailed cmd) {
        final String workId = cmd.workId;
        final String workerId = cmd.workerId;
        log.warning("Work workId={} failed by workerId={}", workId, workerId);
        if (jobDatabase.isInProgress(workId)) {
            changeWorkerToIdle(workerId);
            persist(new JobState.JobFailed(workId, cmd.result), event -> {
                jobDatabase = jobDatabase.updated(event);

                //store logs
                storeLogsForJob((Worker.Result) cmd.result);

                //delete pod for this worker
                try {
                    KubernetesService kubernetesService = kubernetesClientsPerSimulation
                        .get(((Worker.Result) cmd.result).job.trackingId);
                    V1Status status = kubernetesService.deleteDeployment(((Worker.Result) cmd.result).job.jobId);
                    if (status.getStatus().equals("Success")) {
                        log.info("Destroyed pod for workId={}", ((Worker.Result) cmd.result).job.jobId);
                        workers.remove(("gatling-worker." + ((Worker.Result) cmd.result).job.jobId));
                    } else {
                        log.error("Unable to delete deployment for workId={}",
                                  ((Worker.Result) cmd.result).job.jobId);
                    }
                } catch (ExecutionException e) {
                    log.error("There is no client for Kubernetes for tranckingID={}", ((Worker.Result) cmd.result).job.trackingId);
                }
            });
        }
    }

    protected void onWorkIsDone(MasterWorkerProtocol.WorkIsDone cmd) {
        MasterWorkerProtocol.WorkIsDone workDone = cmd;
        final String workerId = workDone.workerId;
        final String workId = workDone.workId;
        if (jobDatabase.isDone(workId)) {
            // send ack to worker that result are received
            getSender().tell(new AckKubernetes(workId), getSelf());

        } else if (!jobDatabase.isInProgress(workId)) {
            log.warning("Work for workId={} not in progress, reported as done by worker with workerId={}", workId,
                        workerId);
        } else {
            log.info("Work for workId={} is done by worker with workerId={}", workId, workerId);
            persist(new JobState.JobCompleted(workId, cmd.result), event -> {
                jobDatabase = jobDatabase.updated(event);

                getSender().tell(new AckKubernetes(event.workId), getSelf());

                String trackingId = jobsPerTrackingID.keySet().stream()
                    .filter(key -> jobsPerTrackingID.get(key).contains(cmd.workId)).collect(Collectors.toList())
                    .get(0);
                //if all the work for a simulation is done, generate and store the report
                if (jobsPerTrackingID.get(trackingId).size() == jobDatabase.getCompletedResults(trackingId).size()) {
                    log.info("All work is done for trackingId={}", trackingId);
                    List<Worker.Result> result = jobDatabase.getCompletedResults(trackingId);

                    TaskEvent taskEvent = new TaskEvent();
                    taskEvent.setJobName("gatling");
                    taskEvent.setParameters(new ArrayList<>(Collections.singletonList("-ro")));
                    reportExecutor
                        .tell(new GenerateReport(new GenericMaster.Report(trackingId, taskEvent), result), getSelf());
                }

            });
        }
    }

    protected void onWorkerRequestsWork(MasterWorkerProtocol.WorkerRequestsWork cmd) {
        log.info("Worker requested work: {}", cmd);
        MasterWorkerProtocol.WorkerRequestsWork workReqMsg = cmd;
        final String workerId = workReqMsg.workerId;

        final WorkerState state = workers.get(workerId);
        final Job job = jobDatabase.hasJob(workerId.split("\\.")[1]);

        persist(new JobState.JobStarted(job.jobId, workerId), event -> {
            jobDatabase = jobDatabase.updated(event);
            log.info("Giving worker with workerId={} some taskEvent {}", workerId, event.workId);
            workers.put(workerId, state.copyWithStatus(new Busy(event.workId, workTimeout.fromNow())));
            getSender().tell(job, getSelf());
        });
    }

    protected void onRegisterWorker(MasterWorkerProtocol.RegisterWorker cmd) {
        String workerId = cmd.workerId;
        if (workers.containsKey(workerId)) {
            workers.put(workerId, workers.get(workerId).copyWithRef(getSender()));
        } else {
            log.info("Worker registered with workerId={}", workerId);
            WorkerState workerState = new WorkerState(getSender(), new Idle(workTimeout.fromNow()));
            workers.put(workerId, workerState);
            String jobId = workerId.split("\\.")[1];

            // keep track of registered workers and start giving them jobs when
            // all the workers for a simulation are connected to master
            for (String trackingId : jobsPerTrackingID.keySet()) {
                if (jobsPerTrackingID.get(trackingId).contains(jobId)) {
                    try {
                        if (workersPerTrackingIdCache.asMap().containsKey(trackingId)) {
                            workersPerTrackingIdCache.get(trackingId).add(workerState);
                        } else {
                            workersPerTrackingIdCache.put(trackingId, new ArrayList<>());
                            workersPerTrackingIdCache.get(trackingId).add(workerState);
                        }

                        // when all the workers for a simulation are registered, give them jobs
                        if (workersPerTrackingIdCache.get(trackingId).size() == jobsPerTrackingID.get(trackingId)
                            .size()) {
                            notifySpecificWorkers(jobsPerTrackingID.get(trackingId));
                        }
                    } catch (Exception e) {
                        log.warning("Exception while trying to register a new worker on kubernetes: {}", e);
                    }
                    break;
                }
            }

        }
    }

    /**
     * Use for notify workers when receiving work results from them
     */
    public final static class AckKubernetes implements Serializable {

        final String workId;

        public AckKubernetes(String workId) {
            this.workId = workId;
        }

        public String getWorkId() {
            return workId;
        }

        @Override
        public String toString() {
            return "AckKubernetes{" + "jobId='" + workId + '\'' + '}';
        }

    }

    /**
     * Use for determine which version is running
     */
    public final static class RunningOnKubernetes implements Serializable {

        public boolean isRunningOnKubernetes;

        public RunningOnKubernetes() {
            isRunningOnKubernetes = false;
        }

        public RunningOnKubernetes(boolean isRunningOnKubernetes) {
            this.isRunningOnKubernetes = isRunningOnKubernetes;
        }

        @Override
        public String toString() {
            if (this.isRunningOnKubernetes) {
                return "Actor system is running on Kubernetes";
            } else {
                return "Actor system is not running on Kubernetes";
            }
        }
    }
}
