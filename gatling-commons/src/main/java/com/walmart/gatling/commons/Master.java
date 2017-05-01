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
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.client.ClusterClientReceptionist;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.persistence.AbstractPersistentActor;
import jersey.repackaged.com.google.common.collect.ImmutableList;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import scala.collection.JavaConversions;
import scala.concurrent.duration.Deadline;
import scala.concurrent.duration.FiniteDuration;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Master extends AbstractPersistentActor {

    public static final Object CleanupTick = new Object() {
        @Override
        public String toString() {
            return "CleanupTick";
        }
    };
    private final ActorRef reportExecutor;
    private final FiniteDuration workTimeout;
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final Cancellable cleanupTask;
    private final AgentConfig agentConfig;
    private HashMap<String, WorkerState> workers = new HashMap<>();
    private Set<String> fileTracker = new HashSet<>();
    private JobState jobDatabase = new JobState();
    private Map<String, UploadFile> fileDatabase = new HashMap<>();
    private Set<String> cancelRequests = new HashSet<>();

    public Master(FiniteDuration workTimeout, AgentConfig agentConfig) {
        this.workTimeout = workTimeout;
        this.agentConfig = agentConfig;
        this.reportExecutor = getContext().watch(getContext().actorOf(Props.create(ReportExecutor.class, agentConfig), "report"));
        ClusterClientReceptionist.get(getContext().system()).registerService(getSelf());
        this.cleanupTask = getContext().system().scheduler().schedule(workTimeout.div(2), workTimeout.div(2), getSelf(), CleanupTick, getContext().dispatcher(), getSelf());
    }

    public static Props props(FiniteDuration workTimeout, AgentConfig agentConfig) {
        return Props.create(Master.class, workTimeout, agentConfig);
    }

    @Override
    public void postStop() {
        cleanupTask.cancel();
    }

    private void notifyWorkers() {
        if (jobDatabase.hasJob()) {
            // could pick a few random instead of all
            for (WorkerState state : workers.values()) {
                if (state.status.isIdle())
                    state.ref.tell(MasterWorkerProtocol.WorkIsReady.getInstance(), getSelf());
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
        return "master";

    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                .match(JobDomainEvent.class, p->{
                    jobDatabase = jobDatabase.updated(p);
                    log.info("Replayed {}", p.getClass().getSimpleName());
                })
                .match(UploadFile.class,p->{
                    log.info("Replayed {}", p.getClass().getSimpleName());
                })
                .build();
    }

    @Override
    public Receive createReceive() {
        return  receiveBuilder()
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
                .match(JobSummaryInfo.class, cmd -> onJobSummary())
                .matchEquals(CleanupTick, cmd -> onCleanupTick())
                .matchAny(cmd -> unhandled(cmd))
                .build();
    }


    private void onJobSummary() {
        getSender().tell(ImmutableList.copyOf(jobDatabase.getJobSummary().values()), getSelf());
    }

    private void onCleanupTick() {
        Iterator<Map.Entry<String, WorkerState>> iterator = workers.entrySet().iterator();
        Set<String> tobeRemoved = new HashSet<>();
        while (iterator.hasNext()) {
            Map.Entry<String, WorkerState> entry = iterator.next();
            String workerId = entry.getKey();
            WorkerState state = entry.getValue();
            if (state.status.isBusy()) {
                if (state.status.getDeadLine().isOverdue()) {
                    log.info("Work timed out: {}", state.status.getWorkId());
                    tobeRemoved.add(workerId);
                    persist(new JobState.JobTimedOut(state.status.getWorkId()), event -> {
                        // remove from in progress to pending
                        jobDatabase = jobDatabase.updated(event);
                        notifyWorkers();
                    });
                }
            }
        }
        for (String workerId : tobeRemoved) {
            workers.remove(workerId);
        }

    }

    private void onJob(Job cmd) {
        final String workId = cmd.jobId;
        // idempotent
        if (jobDatabase.isAccepted(workId)) {
            getSender().tell(new Ack(workId), getSelf());
        } else {
            log.info("Accepted work: {}", workId);
            persist(new JobState.JobAccepted(cmd), event -> {
                // Ack back to original sender
                getSender().tell(new Ack(event.job.jobId), getSelf());
                jobDatabase = jobDatabase.updated(event);
                notifyWorkers();
            });
        }
    }

    private void onUploadFile(Object cmd) {
        log.info("Accepted upload file request: {}", cmd);
        UploadFile request = (UploadFile) cmd;
        persist(request, event -> {
            getSender().tell(new Ack(request.trackingId), getSelf());
            fileDatabase.put(request.trackingId, request);
        });
    }

    private void onReport(Object cmd) {
        log.info("Accepted tracking info request: {}", cmd);
        List<Worker.Result> result = jobDatabase.getCompletedResults(((Report) cmd).trackingId);
        reportExecutor.forward(new GenerateReport((Report) cmd, result), getContext());
    }

    private void onTrackingInfo(Object cmd) {
        TrackingInfo trackingInfo = (TrackingInfo) cmd;
        log.info("Accepted tracking info request: {}", cmd);
        TrackingResult result = jobDatabase.getTrackingInfo(trackingInfo.trackingId);
        log.info("Complete tracking info request: {}", result);
        if(trackingInfo.cancel) {
            cancelRequests.add(trackingInfo.trackingId);
        }
        result.setCancelled(cancelRequests.contains(trackingInfo.trackingId));
        getSender().tell(result, getSelf());
    }

    private void onServerInfo(Object cmd) {
        log.info("Accepted Server info request: {}", cmd);
        getSender().tell(new ServerInfo(workers), getSelf());
    }

    private void onUploadInfo(Object cmd) {
        log.info("Accepted Upload info request: {}", cmd);
        UploadInfo info = (UploadInfo) cmd;
        info.setHosts(fileTracker.stream().filter(p -> p.contains(info.trackingId)).map(p -> p.split("_")[1]).collect(Collectors.toList()));
        getSender().tell(info, getSelf());
    }

    private void onWorkFailed(MasterWorkerProtocol.WorkFailed cmd) {
        final String workId = cmd.workId;
        final String workerId = cmd.workerId;
        log.info("Work {} failed by worker {}", workId, workerId);
        if (jobDatabase.isInProgress(workId)) {
            changeWorkerToIdle(workerId, workId);
            persist(new JobState.JobFailed(workId, cmd.result), event -> {
                jobDatabase = jobDatabase.updated(event);
                notifyWorkers();
            });
        }
    }

    private void onWorkIsDone(MasterWorkerProtocol.WorkIsDone cmd) {
        MasterWorkerProtocol.WorkIsDone workDone = cmd;
        final String workerId = workDone.workerId;
        final String workId = workDone.workId;
        if (jobDatabase.isDone(workId)) {
            getSender().tell(new Ack(workId), getSelf());
        } else if (!jobDatabase.isInProgress(workId)) {
            log.info("Work {} not in progress, reported as done by worker {}", workId, workerId);
        } else {
            log.info("Work {} is done by worker {}", workId, workerId);
            changeWorkerToIdle(workerId, workId);
            persist(new JobState.JobCompleted(workId, cmd.result), event -> {
                jobDatabase = jobDatabase.updated(event);
                getSender().tell(new Ack(event.workId), getSelf());
            });
        }
    }

    private void onWorkInProgress(MasterWorkerProtocol.WorkInProgress cmd) {
        final String workerId = cmd.workerId;
        final String workId = cmd.workId;
        final WorkerState state = workers.get(workerId);
        if (jobDatabase.isInProgress(workId)) {
            if (state != null && state.status.isBusy()) {
                workers.put(workerId, state.copyWithStatus(new Busy(state.status.getWorkId(), workTimeout.fromNow())));
            }
        } else {
            log.info("Work {} not in progress, reported as in progress by worker {}", workId, workerId);
        }
    }

    private void onFileUploadComplete(Worker.FileUploadComplete cmd) {
        Worker.FileUploadComplete result = cmd;
        fileTracker.add(result.result.trackingId + "_" + result.host);
    }

    private void onWorkerRequestsWork(Object cmd) {
        log.info("Worker requested work: {}", cmd);
        if (jobDatabase.hasJob()) {
            MasterWorkerProtocol.WorkerRequestsWork workReqMsg = ((MasterWorkerProtocol.WorkerRequestsWork) cmd);
            final String workerId = workReqMsg.workerId;
            final WorkerState state = workers.get(workerId);
            if (state != null && state.status.isIdle()) {
                //for (int i=0; i<jobDatabase.getPendingJobsCount(); i++) {
                    final Job job = jobDatabase.nextJob();//nextJob for the partition/role
                    boolean jobWorkerRoleMatched = workReqMsg.role.equalsIgnoreCase(job.roleId);
                if (jobWorkerRoleMatched) {
                    persist(new JobState.JobStarted(job.jobId, workerId), event -> {
                        jobDatabase = jobDatabase.updated(event);
                        log.info("Giving worker {} some taskEvent {}", workerId, event.workId);
                        workers.put(workerId, state.copyWithStatus(new Busy(event.workId, workTimeout.fromNow())));
                        getSender().tell(job, getSelf());
                    });
                } else {
                    persist(new JobState.JobPostponed(job.jobId), event -> {
                        jobDatabase = jobDatabase.updated(event);
                        log.info("Postponing work: {}", workerId);
                        });
                    }
                //}
            }
        }
    }

    private void onWorkerRequestsFile(MasterWorkerProtocol.WorkerRequestsFile cmd) throws IOException {
        MasterWorkerProtocol.WorkerRequestsFile msg = cmd;
        Set<String> trackingIds = fileDatabase.keySet();
        Optional<String> unsentFile = trackingIds.stream().map(p -> p.concat("_").concat(msg.host)).filter(p -> !fileTracker.contains(p)).findFirst();
        if (unsentFile.isPresent()) {
            String tId = unsentFile.get().split("_")[0];
            UploadFile uploadFile = fileDatabase.get(tId);
            if(uploadFile.type.equalsIgnoreCase("lib")){
                String soonToBeRemotePath = agentConfig.getMasterUrl(uploadFile.path);
                getSender().tell(new FileJob(null, uploadFile, soonToBeRemotePath), getSelf());
            }
            else {
                String content = FileUtils.readFileToString(new File(uploadFile.path));
                getSender().tell(new FileJob(content, uploadFile, null), getSelf());
            }
        }
    }

    private void onRegisterWorker(MasterWorkerProtocol.RegisterWorker cmd) {
        String workerId = cmd.workerId;
        if (workers.containsKey(workerId)) {
            workers.put(workerId, workers.get(workerId).copyWithRef(getSender()));
        } else {
            log.info("Worker registered: {}", workerId);
            workers.put(workerId, new WorkerState(getSender(), Idle.instance));
            if (jobDatabase.hasJob()) {
                getSender().tell(MasterWorkerProtocol.WorkIsReady.getInstance(), getSelf());
            }
        }
    }

    private void changeWorkerToIdle(String workerId, String workId) {
        if (workers.get(workerId).status.isBusy()) {
            workers.put(workerId, workers.get(workerId).copyWithStatus(new Idle()));
        }
    }

    public static abstract class WorkerStatus {
        protected abstract boolean isIdle();

        private boolean isBusy() {
            return !isIdle();
        }

        protected abstract String getWorkId();

        protected abstract Deadline getDeadLine();
    }

    private static final class Idle extends WorkerStatus {
        private static final Idle instance = new Idle();

        public static Idle getInstance() {
            return instance;
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
            throw new IllegalAccessError();
        }

        @Override
        public String toString() {
            return "Idle";
        }
    }

    private static final class Busy extends WorkerStatus {
        private final String workId;
        private final Deadline deadline;

        private Busy(String workId, Deadline deadline) {
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
            return "Busy{" + "work=" + workId + ", deadline=" + deadline + '}';
        }
    }

    public static final class WorkerState {
        public final ActorRef ref;
        public final WorkerStatus status;

        private WorkerState(ActorRef ref, WorkerStatus status) {
            this.ref = ref;
            this.status = status;
        }

        private WorkerState copyWithRef(ActorRef ref) {
            return new WorkerState(ref, this.status);
        }

        private WorkerState copyWithStatus(WorkerStatus status) {
            return new WorkerState(this.ref, status);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || !getClass().equals(o.getClass()))
                return false;

            WorkerState that = (WorkerState) o;

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

    public static final class Job implements Serializable {
        public final Object taskEvent;//task
        public final String jobId;
        public final String roleId;
        public final String trackingId;
        public String abortUrl;

        public Job(String roleId, Object job, String trackingId,String abortUrl) {
            this.jobId = UUID.randomUUID().toString();
            this.roleId = roleId;
            this.taskEvent = job;
            this.trackingId = trackingId;
            this.abortUrl = abortUrl;
        }

        @Override
        public String toString() {
            return "Job{" +
                    "taskEvent=" + taskEvent +
                    ", jobId='" + jobId + '\'' +
                    ", roleId='" + roleId + '\'' +
                    ", trackingId='" + trackingId + '\'' +
                    '}';
        }
    }

    public static final class FileJob implements Serializable {
        public final String content;//task
        public final String remotePath;//task
        public final String jobId;
        public final UploadFile uploadFileRequest;

        public FileJob(String content, UploadFile uploadFileRequest,String remotePath) {
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

    public static final class UploadFile implements Serializable {
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
            if(type.equalsIgnoreCase("conf") || type.equalsIgnoreCase("lib"))
                return type + "/" + name;
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

    public static final class Report implements Serializable {
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

    public static final class GenerateReport implements Serializable {
        public final Report reportJob;
        public final List<Worker.Result> results;

        public GenerateReport(Report repotJob, List<Worker.Result> results) {
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

    public static final class TrackingInfo implements Serializable {
        public final String trackingId;
        public final boolean cancel;

        public TrackingInfo(String trackingId) {
            this.trackingId = trackingId;
            this.cancel = false;
        }
        public TrackingInfo(String trackingId,boolean cancel) {
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

    public static final class UploadInfo implements Serializable {
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

    public static final class JobSummaryInfo implements Serializable {
    }

    public static final class ServerInfo implements Serializable {

        private ImmutableMap<String, WorkerState> workers;

        public ServerInfo() {
        }

        public ServerInfo(HashMap<String, WorkerState> workers) {
            this.workers = ImmutableMap.copyOf(workers);
        }

        public ImmutableMap<String, WorkerState> getWorkers() {
            return workers == null? ImmutableMap.of():workers;
        }

        @Override
        public String toString() {
            return "ServerInfo{" +
                    "workers=" + getWorkers() +
                    '}';
        }
    }

    public static final class Ack implements Serializable {
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
}
