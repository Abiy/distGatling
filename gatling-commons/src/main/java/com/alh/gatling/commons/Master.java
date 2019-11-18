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

package com.alh.gatling.commons;

import com.alh.gatling.commons.exception.NotFoundException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.cluster.Cluster;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.Recovery;
import jersey.repackaged.com.google.common.collect.ImmutableList;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import scala.collection.JavaConversions;
import scala.concurrent.duration.Deadline;
import scala.concurrent.duration.FiniteDuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class Master extends AbstractPersistentActor {

    public static final Object CleanupTick = new Object() {
        @Override
        public String toString() {
            return "CleanupTick";
        }
    };
    protected ActorRef reportExecutor;
    protected FiniteDuration workTimeout;
    protected LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    protected Cancellable cleanupTask;
    protected AgentConfig agentConfig;
    protected HashMap<String, WorkerState> workers = new HashMap<>();
    protected Set<String> fileTracker = new HashSet<>();
    protected JobState jobDatabase = new JobState();
    protected Map<String, UploadFile> fileDatabase = new HashMap<>();
    protected Set<String> cancelRequests = new HashSet<>();


    @Override
    public void postStop() {
        cleanupTask.cancel();
    }

    private void notifyWorkers() {
        if (jobDatabase.hasJob()) {
            // could pick a few random instead of all
            for (Master.WorkerState state : workers.values()) {
                if (state.status.isIdle()) {
                    state.ref.tell(MasterWorkerProtocol.WorkIsReady.getInstance(), getSelf());
                }
            }
        }
    }

    protected ReceiveBuilder createReceiveBuilder() {
        return receiveBuilder()
            .match(MasterWorkerProtocol.RegisterWorker.class, cmd -> onRegisterWorker(cmd))
            .match(MasterWorkerProtocol.WorkerRequestsFile.class, cmd -> onWorkerRequestsFile(cmd))
            .match(MasterWorkerProtocol.WorkerRequestsWork.class, cmd -> onWorkerRequestsWork(cmd))
            .match(Worker.FileUploadComplete.class, cmd -> onFileUploadComplete(cmd))
            .match(MasterWorkerProtocol.WorkInProgress.class, cmd -> onWorkInProgress(cmd))
            .match(MasterWorkerProtocol.WorkIsDone.class, cmd -> onWorkIsDone(cmd))
            .match(MasterWorkerProtocol.WorkFailed.class, cmd -> onWorkFailed(cmd))
            .match(UploadInfo.class, cmd -> onUploadInfo(cmd))
            .match(ServerInfo.class, cmd -> onServerInfo(cmd))
            .match(TrackingInfo.class, cmd -> onTrackingInfo(cmd))
            .match(Report.class, cmd -> onReport(cmd))
            .match(UploadFile.class, cmd -> onUploadFile(cmd))
            .match(Job.class, cmd -> onJob(cmd))
            .match(MasterClientProtocol.CommandLineJob.class, cmd -> processCmdLineJob(cmd))
            .match(JobSummaryInfo.class, cmd -> onJobSummary())
            .match(KubernetesMaster.RunningOnKubernetes.class, cmd -> onCheckRunningOnKubernetes());
    }


    @Override
    public String persistenceId() {
        for (String role : JavaConversions.asJavaIterable((Cluster.get(getContext().system()).selfRoles()))) {
            if (role.startsWith("backend-")) {
                return role + "-master";
            }
        }
        return "master";

    }

    @Override
    public Recovery recovery() {
        return Recovery.create(100);
    }

    @Override
    public AbstractActor.Receive createReceiveRecover() {
        return receiveBuilder()
            .match(JobDomainEvent.class, p -> {
                jobDatabase = jobDatabase.updated(p);
                log.info("Replayed {}", p.getClass().getSimpleName());
            })
            .match(Master.UploadFile.class, p -> {
                log.info("Replayed {}", p.getClass().getSimpleName());
            })
            .build();
    }

    protected abstract void onCheckRunningOnKubernetes();

    protected abstract void onJob(Master.Job cmd);

    protected abstract void onReport(Object cmd);

    protected abstract void onWorkFailed(MasterWorkerProtocol.WorkFailed cmd);

    protected abstract void onWorkIsDone(MasterWorkerProtocol.WorkIsDone cmd);

    protected abstract void onWorkerRequestsWork(MasterWorkerProtocol.WorkerRequestsWork cmd);

    protected abstract void onRegisterWorker(MasterWorkerProtocol.RegisterWorker cmd);

    protected void onJobSummary() {
        getSender().tell(ImmutableList.copyOf(jobDatabase.getJobSummary().values()), getSelf());
    }

    protected void onCleanupTick() {
        Iterator<Map.Entry<String, WorkerState>> iterator = workers.entrySet().iterator();
        Set<String> tobeRemoved = new HashSet<>();
        while (iterator.hasNext()) {
            Map.Entry<String, Master.WorkerState> entry = iterator.next();
            String workerId = entry.getKey();
            Master.WorkerState state = entry.getValue();
            if (state.status.isBusy()) {
                if (state.status.getDeadLine().isOverdue()) {
                    log.info("Work timed out for workId={}", state.status.getWorkId());
                    tobeRemoved.add(workerId);
                    persist(new JobState.JobTimedOut(state.status.getWorkId()), event -> {
                        // remove from in progress to pending
                        jobDatabase = jobDatabase.updated(event);
                        notifyWorkers();
                    });
                }
            } else {
                if (state.status.getDeadLine().isOverdue()) { //we missed pings from worker or worker is dead
                    tobeRemoved.add(workerId);
                }
            }
        }
        for (String workerId : tobeRemoved) {
            workers.remove(workerId);
        }

    }

    protected Optional<String> processCmdLineJob(MasterClientProtocol.CommandLineJob cmdJob) {
        ClientConfig clientConfig = cmdJob.clientConfig;
        getSender().tell(new Master.Ack(cmdJob.clientId), getSelf());
        String trackingId = UUID.randomUUID().toString();
        List<String> parameters = Arrays.asList(clientConfig.getParameterString().split(" "));
        boolean hasResourcesFeed = !clientConfig.getResourcesFeedPath().isEmpty();
        JobSummary.JobInfo jobinfo = JobSummary.JobInfo.newBuilder()
            .withCount(clientConfig.getParallelism())
            .withJobName("gatling")
            .withPartitionAccessKey(clientConfig.getAccessKey())
            .withPartitionName(clientConfig.getPartitionName())
            .withUser(clientConfig.getUserName())
            .withTrackingId(trackingId)
            .withHasResourcesFeed(hasResourcesFeed)
            .withParameterString(clientConfig.getParameterString())
            .withFileFullName(clientConfig.getJarFileName())//class name is not fileName
            .withResourcesFileName((clientConfig.getResourcesFeedFileName()))
            .withJarFileName(clientConfig.getJarFileName())
            .build();
        for (int i = 0; i < clientConfig.getParallelism(); i++) {
            TaskEvent taskEvent = new TaskEvent();
            taskEvent.setJobName("gatling"); //the gatling.sh script is the gateway for simulation files
            taskEvent.setJobInfo(jobinfo);
            taskEvent.setParameters(new ArrayList<>(parameters));
            Master.Job job = new Master.Job(clientConfig.getPartitionName(), taskEvent, trackingId,
                                            agentConfig.getAbortUrl(),
                                            agentConfig.getJobFileUrl(clientConfig.getJarPath()),
                                            agentConfig
                                                .getJobFileUrl(clientConfig.getResourcesFeedPath()),
                                            true);
            persist(new JobState.JobAccepted(job), event -> {
                // Ack back to original sender
                getSender().tell(new MasterClientProtocol.CommandLineJobAccepted(job), getSelf());
                jobDatabase = jobDatabase.updated(event);
                notifyWorkers();
            });

        }
        getSender().tell(new MasterClientProtocol.CommandLineJobSubmitted(trackingId), getSelf());
        return Optional.of(trackingId);
    }


    protected void onUploadFile(Object cmd) {
        log.info("Accepted upload file request: {}", cmd);
        Master.UploadFile request = (Master.UploadFile) cmd;
        persist(request, event -> {
            getSender().tell(new Master.Ack(request.trackingId), getSelf());
            fileDatabase.put(request.trackingId, request);
        });
    }


    protected void onTrackingInfo(Object cmd) {
        Master.TrackingInfo trackingInfo = (Master.TrackingInfo) cmd;
        log.info("Accepted tracking info request for trackingId={}: {}", trackingInfo.trackingId, cmd);
        TrackingResult result = jobDatabase.getTrackingInfo(trackingInfo.trackingId);
        log.info("Complete tracking info request for trackingId={}: {} ", trackingInfo.trackingId, result);
        if (trackingInfo.cancel) {
            cancelRequests.add(trackingInfo.trackingId);
        }
        result.setCancelled(cancelRequests.contains(trackingInfo.trackingId));
        getSender().tell(result, getSelf());
    }

    protected void onServerInfo(Object cmd) {
        log.info("Accepted Server info request: {}", cmd);
        getSender().tell(new Master.ServerInfo(workers), getSelf());
    }

    protected void onUploadInfo(Object cmd) {
        log.info("Accepted Upload info request: {}", cmd);
        Master.UploadInfo info = (Master.UploadInfo) cmd;
        info.setHosts(fileTracker.stream().filter(p -> p.contains(info.trackingId)).map(p -> p.split("_")[1])
                          .collect(Collectors.toList()));
        getSender().tell(info, getSelf());
    }


    protected void onWorkInProgress(MasterWorkerProtocol.WorkInProgress cmd) {
        final String workerId = cmd.workerId;
        final String workId = cmd.workId;
        final Master.WorkerState state = workers.get(workerId);
        if (jobDatabase.isInProgress(workId)) {
            if (state != null && state.status.isBusy()) {
                workers.put(workerId, state
                    .copyWithStatus(new Master.Busy(state.status.getWorkId(), workTimeout.fromNow())));
            }
        } else {
            log.info("Work with workId={} not in progress, reported as in progress by worker with workerId={}", workId,
                     workerId);
        }
    }

    protected void onFileUploadComplete(Worker.FileUploadComplete cmd) {
        Worker.FileUploadComplete result = cmd;
        fileTracker.add(result.result.trackingId + "_" + result.host);
    }


    protected void extendIdleExpiryTime(String workerId) {
        if (workers.get(workerId).status.isIdle()) {
            workers.put(workerId, workers.get(workerId).copyWithStatus(new Master.Idle(workTimeout.fromNow())));
        }
    }

    protected void onWorkerRequestsFile(MasterWorkerProtocol.WorkerRequestsFile cmd) throws IOException {
        MasterWorkerProtocol.WorkerRequestsFile msg = cmd;
        Set<String> trackingIds = fileDatabase.keySet();
        Optional<String> unsentFile = trackingIds.stream().map(p -> p.concat("_").concat(msg.host))
            .filter(p -> !fileTracker.contains(p)).findFirst();
        if (unsentFile.isPresent()) {
            String tId = unsentFile.get().split("_")[0];
            Master.UploadFile uploadFile = fileDatabase.get(tId);
            if (uploadFile.type.equalsIgnoreCase("lib")) {
                String soonToBeRemotePath = agentConfig.getMasterUrl(uploadFile.path);
                getSender().tell(new Master.FileJob(null, uploadFile, soonToBeRemotePath), getSelf());
            } else {
                String content = FileUtils.readFileToString(new File(uploadFile.path));
                getSender().tell(new Master.FileJob(content, uploadFile, null), getSelf());
            }
        }
    }


    protected void changeWorkerToIdle(String workerId) {
        if (workers.get(workerId).status.isBusy()) {
            workers.put(workerId, workers.get(workerId).copyWithStatus(new Master.Idle(workTimeout.fromNow())));
        }
    }

    /**
     * Load logs from a path from workers
     *
     * @param logPath path on a worker where logs are stored
     * @return logs from that workers in String format
     */
    public static String loadLogFromPath(String logPath) throws NotFoundException {
        URL url = null;
        try {
            url = new URL(logPath);
        } catch (MalformedURLException e) {
            throw new NotFoundException();
        }
        try (InputStream input = url.openStream()) {
            try {
                return IOUtils.toString(input, StandardCharsets.UTF_8.toString());
            } catch (Exception e) {
                throw new NotFoundException();
            }

        } catch (IOException e) {
            throw new NotFoundException();
        }
    }


    public final static class WorkerState {

        public final ActorRef ref;
        public final Master.WorkerStatus status;

        protected WorkerState(ActorRef ref, Master.WorkerStatus status) {
            this.ref = ref;
            this.status = status;
        }

        protected Master.WorkerState copyWithRef(ActorRef ref) {
            return new Master.WorkerState(ref, this.status);
        }

        protected Master.WorkerState copyWithStatus(Master.WorkerStatus status) {
            return new Master.WorkerState(this.ref, status);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || !getClass().equals(o.getClass())) {
                return false;
            }

            Master.WorkerState that = (Master.WorkerState) o;

            return ref.equals(that.ref) && status.equals(that.status);

        }

        @Override
        public int hashCode() {
            int result = ref.hashCode();
            result = 31 * result + status.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "WorkerState{" + "ref=" + ref + ", status=" + status + '}';
        }
    }

    public final static class Job implements Serializable {

        public final Object taskEvent;//task
        public final String jobId;
        public final String roleId;
        public final String trackingId;
        public final boolean isJarSimulation;
        public String abortUrl;
        public String jobFileUrl;
        public String resourcesFileUrl;
        public int expectedWorkers;

        public Job(String roleId, Object job, String trackingId, String abortUrl, String jobFileUrl,
                   String resourcesFileUrl, boolean isJarSimulation) {
            this.jobId = UUID.randomUUID().toString();
            this.roleId = roleId;
            this.taskEvent = job;
            this.trackingId = trackingId;
            this.abortUrl = abortUrl;
            this.jobFileUrl = jobFileUrl;
            this.resourcesFileUrl = resourcesFileUrl;
            this.isJarSimulation = isJarSimulation;
        }

        public Job(String roleId, Object job, String trackingId, String abortUrl, String jobFileUrl,
                   String resourcesFileUrl, boolean isJarSimulation, int expectedWorkers) {
            this(roleId, job, trackingId, abortUrl, jobFileUrl, resourcesFileUrl, isJarSimulation);
            this.expectedWorkers = expectedWorkers;
        }

        @Override
        public String toString() {
            return "Job{" +
                   "taskEvent=" + taskEvent +
                   ", jobId='" + jobId + '\'' +
                   ", roleId='" + roleId + '\'' +
                   ", trackingId='" + trackingId + '\'' +
                   ", isJarSimulation=" + isJarSimulation +
                   ", abortUrl='" + abortUrl + '\'' +
                   ", jobFileUrl='" + jobFileUrl + '\'' +
                   ", resourcesFileUrl='" + resourcesFileUrl + '\'' +
                   '}';
        }
    }

    public final static class SubmitNewSimulation implements Serializable {
        public KubernetesService kubernetesService;
        public String trackingId;

        public SubmitNewSimulation(String trackingID, KubernetesService kubernetesService){
            this.trackingId = trackingID;
            this.kubernetesService = kubernetesService;
        }
    }

    public final static class FileJob implements Serializable {

        public final String content;//task
        public final String remotePath;//task
        public final String jobId;
        public final Master.UploadFile uploadFileRequest;

        public FileJob(String content, Master.UploadFile uploadFileRequest, String remotePath) {
            this.jobId = UUID.randomUUID().toString();
            this.uploadFileRequest = uploadFileRequest;
            this.content = content;
            this.remotePath = remotePath;
        }

        @Override
        public String toString() {
            return "FileJob{" +
                   "content='" + content + '\'' +
                   ", remotePath='" + remotePath + '\'' +
                   ", jobId='" + jobId + '\'' +
                   ", uploadFileRequest=" + uploadFileRequest +
                   '}';
        }
    }

    public final static class UploadFile implements Serializable {

        public final String trackingId;
        public final String path, name, role, type;

        public UploadFile(String trackingId, String path, String name, String role, String type) {
            this.trackingId = trackingId;
            this.path = path;
            this.name = name;
            this.role = role;
            this.type = type;
        }

        public String getFileName() {
            if (type.equalsIgnoreCase("conf") || type.equalsIgnoreCase("lib")) {
                return type + "/" + name;
            }
            return "user-files/" + type + "/" + name;
        }

        @Override
        public String toString() {
            return "UploadFile{" +
                   "trackingId='" + trackingId + '\'' +
                   ", path='" + path + '\'' +
                   ", name='" + name + '\'' +
                   ", role='" + role + '\'' +
                   ", type='" + type + '\'' +
                   '}';
        }
    }

    public final static class Report implements Serializable {

        public final String trackingId;
        public final TaskEvent taskEvent;

        public Report(String trackingId, TaskEvent taskEvent) {
            this.trackingId = trackingId;
            this.taskEvent = taskEvent;
        }

        @Override
        public String toString() {
            return "Report{" +
                   "trackingId='" + trackingId + '\'' +
                   '}';
        }

        public String getHtml() {
            return "/resources/" + trackingId + "/index.html";
        }
    }

    public final static class GenerateReport implements Serializable {

        public final Master.Report reportJob;
        public final List<Worker.Result> results;

        public GenerateReport(Master.Report repotJob, List<Worker.Result> results) {
            this.reportJob = repotJob;
            this.results = results;
        }

        @Override
        public String toString() {
            return "GenerateReport{" +
                   "reportJob=" + reportJob +
                   ", results=" + results +
                   '}';
        }
    }

    public final static class TrackingInfo implements Serializable {

        public final String trackingId;
        public final boolean cancel;

        public TrackingInfo(String trackingId) {
            this.trackingId = trackingId;
            this.cancel = false;
        }

        public TrackingInfo(String trackingId, boolean cancel) {
            this.trackingId = trackingId;
            this.cancel = cancel;
        }

        @Override
        public String toString() {
            return "TrackingInfo{" +
                   "trackingId='" + trackingId + '\'' +
                   ", cancel=" + cancel +
                   '}';
        }
    }

    public final static class UploadInfo implements Serializable {

        public final String trackingId;
        public List<String> hosts;

        public UploadInfo(String trackingId) {
            this.trackingId = trackingId;
        }

        public void setHosts(List<String> hosts) {
            this.hosts = hosts;
        }

        @Override
        public String toString() {
            return "UploadInfo{" +
                   "trackingId='" + trackingId + '\'' +
                   ", hosts=" + hosts +
                   '}';
        }
    }

    public final static class JobSummaryInfo implements Serializable {

    }

    public final static class ServerInfo implements Serializable {

        private ImmutableMap<String, WorkerState> workers;

        public ServerInfo() {
        }

        public ServerInfo(HashMap<String, Master.WorkerState> workers) {
            this.workers = ImmutableMap.copyOf(workers);
        }

        public ImmutableMap<String, Master.WorkerState> getWorkers() {
            return workers == null ? ImmutableMap.of() : workers;
        }

        @Override
        public String toString() {
            return "ServerInfo{" +
                   "workers=" + getWorkers() +
                   '}';
        }
    }

    public final static class Ack implements Serializable {

        final String workId;

        public Ack(String workId) {
            this.workId = workId;
        }

        public String getWorkId() {
            return workId;
        }

        @Override
        public String toString() {
            return "Ack{" + "jobId='" + workId + '\'' + '}';
        }
    }

    /**
     * Store logs from a worker
     */
    public final static class WorkerLog implements Serializable {

        public String errorLog;
        public String stdLog;

        public WorkerLog(String errorLog, String stdLog) {
            this.errorLog = errorLog;
            this.stdLog = stdLog;
        }
    }

    /**
     * Use for asking for a worker logs
     */
    public final static class JobLogs implements Serializable {

        String jobId;
        String trackingId;
        Master.WorkerLog workerLog;

        public JobLogs(String trackingId, String jobId) {
            this.jobId = jobId;
            this.trackingId = trackingId;
        }

        public JobLogs(String trackingId, String jobId, Master.WorkerLog workerLog) {
            this.jobId = jobId;
            this.trackingId = trackingId;
            this.workerLog = workerLog;
        }

        public Master.WorkerLog getWorkerLog() {
            return this.workerLog;
        }

        public void setWorkerLog(Master.WorkerLog workerLog) {
            this.workerLog = workerLog;
        }

        public String getJobId() {
            return this.jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public String getTrackingId() {
            return this.trackingId;
        }

        public void setTrackingId(String trackingId) {
            this.trackingId = trackingId;
        }
    }

    public abstract class WorkerStatus {

        protected abstract boolean isIdle();

        protected boolean isBusy() {
            return !isIdle();
        }

        protected abstract String getWorkId();

        protected abstract Deadline getDeadLine();
    }

    public final class Idle extends Master.WorkerStatus {

        private final Deadline deadline;

        protected Idle(Deadline deadline) {
            this.deadline = deadline;
        }

        @Override
        protected boolean isIdle() {
            return true;
        }

        @Override
        protected String getWorkId() {
            throw new IllegalAccessError();
        }

        @Override
        protected Deadline getDeadLine() {
            return deadline;
        }

        @Override
        public String toString() {
            return "Idle";
        }
    }

    public final class Busy extends Master.WorkerStatus {

        private final String workId;
        private final Deadline deadline;

        protected Busy(String workId, Deadline deadline) {
            this.workId = workId;
            this.deadline = deadline;
        }

        @Override
        protected boolean isIdle() {
            return false;
        }

        @Override
        protected String getWorkId() {
            return workId;
        }

        @Override
        protected Deadline getDeadLine() {
            return deadline;
        }

        @Override
        public String toString() {
            return "Busy";
        }
    }

}