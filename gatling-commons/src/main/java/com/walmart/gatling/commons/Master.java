package com.walmart.gatling.commons;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.client.ClusterClientReceptionist;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.persistence.UntypedPersistentActor;
import scala.collection.JavaConversions;
import scala.concurrent.duration.Deadline;
import scala.concurrent.duration.FiniteDuration;

public class Master extends UntypedPersistentActor {

    public static Props props(FiniteDuration workTimeout) {
        return Props.create(Master.class, workTimeout);
    }

    private final FiniteDuration workTimeout;
    //private final ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final Cancellable cleanupTask;

    private HashMap<String, WorkerState> workers = new HashMap<String, WorkerState>();
    private JobState jobDatabase = new JobState();

    public Master(FiniteDuration workTimeout) {
        this.workTimeout = workTimeout;
        ClusterClientReceptionist.get(getContext().system()).registerService(getSelf());
        this.cleanupTask = getContext().system().scheduler().schedule(workTimeout.div(2), workTimeout.div(2), getSelf(), CleanupTick, getContext().dispatcher(), getSelf());
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

    private static abstract class WorkerStatus {
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

    private static final class WorkerState {
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

    public static final Object CleanupTick = new Object() {
        @Override
        public String toString() {
            return "CleanupTick";
        }
    };

    public static final class Job implements Serializable {
        public final String jobId;
        public final String roleId;
        public final Object taskEvent;//task

        public Job(String jobId, String roleId, Object job) {
            this.jobId = jobId;
            this.roleId = roleId;
            this.taskEvent = job;
        }

        @Override
        public String toString() {
            return "Job{" + "jobId='" + jobId + '\'' + ", taskEvent=" + taskEvent + '}';
        }
    }

    public static final class ServerInfo implements Serializable {
        public final String jobId;
        public final String roleId;

        public ServerInfo(String jobId, String roleId) {
            this.jobId = jobId;
            this.roleId = roleId;
        }

        @Override
        public String toString() {
            return "ServerInfo{" +
                    "jobId='" + jobId + '\'' +
                    ", roleId='" + roleId + '\'' +
                    '}';
        }

    }

    public static final class WorkResult implements Serializable {
        public final Object result;
        public final Job job;

        public WorkResult(Job job, Object result) {
            this.result = result;
            this.job = job;
        }


        @Override
        public String toString() {
            return "WorkResult{" +
                    "result=" + result +
                    ", job=" + job +
                    '}';
        }
    }

    public final static class JobLogEvent implements Serializable {
        public final JobDomainEvent domainEvent;
        public final Job job;

        public JobLogEvent(JobDomainEvent domainEvent, Job job) {

            this.domainEvent = domainEvent;
            this.job = job;
        }

        @Override
        public String toString() {
            return "JobLogEvent{" +
                    "domainEvent=" + domainEvent +
                    ", taskEvent=" + job +
                    '}';
        }
    }

    public static final class Ack implements Serializable {
        final String workId;

        public Ack(String workId) {
            this.workId = workId;
        }

        @Override
        public String toString() {
            return "Ack{" + "jobId='" + workId + '\'' + '}';
        }
    }

    @Override
    public void onReceiveRecover(Object arg0) throws Exception {
        if (arg0 instanceof JobDomainEvent) {
            jobDatabase = jobDatabase.updated((JobDomainEvent) arg0);
            log.info("Replayed {}", arg0.getClass().getSimpleName());
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
    public void onReceiveCommand(Object cmd) throws Exception {

        System.out.println("MESSAGE IS BEING PROCESSED ON THE MASTER...............");
        if (cmd instanceof MasterWorkerProtocol.RegisterWorker) {
            System.out.println("MESSAGE IS BEING PROCESSED FOR REGISTERING NEW WORKER...............");
            String workerId = ((MasterWorkerProtocol.RegisterWorker) cmd).workerId;
            if (workers.containsKey(workerId)) {
                workers.put(workerId, workers.get(workerId).copyWithRef(getSender()));
            } else {
                log.info("Worker registered: {}", workerId);
                workers.put(workerId, new WorkerState(getSender(), Idle.instance));
                if (jobDatabase.hasJob()) {
                    getSender().tell(MasterWorkerProtocol.WorkIsReady.getInstance(), getSelf());
                }
            }
        } else if (cmd instanceof MasterWorkerProtocol.WorkerRequestsWork) {
            if (jobDatabase.hasJob()) {
                MasterWorkerProtocol.WorkerRequestsWork msg = ((MasterWorkerProtocol.WorkerRequestsWork) cmd);
                final String workerId = msg.workerId;
                final WorkerState state = workers.get(workerId);
                if (state!=null && state.status.isIdle()) {
                    final Job job = jobDatabase.nextJob();
                    boolean jobWorkerRoleMatched  = msg.role.equalsIgnoreCase(job.roleId)  ;
                    if (jobWorkerRoleMatched) {

                    }
                    else {

                    }
                }
            }
        } else if (cmd instanceof MasterWorkerProtocol.WorkInProgress) {
            final String workerId = ((MasterWorkerProtocol.WorkInProgress) cmd).workerId;
            final String workId = ((MasterWorkerProtocol.WorkInProgress) cmd).workId;
            final WorkerState state = workers.get(workerId);
            if(jobDatabase.isInProgress(workId)) {
                if (state!=null && state.status.isBusy()) {
                    workers.put(workerId, state.copyWithStatus(new Busy(state.status.getWorkId(), workTimeout.fromNow())));
                }
            }
            else {
                log.info("Work {} not in progress, reported as in progress by worker {}", workId, workerId);
            }

        } else if (cmd instanceof MasterWorkerProtocol.WorkIsDone) {
            final String workerId = ((MasterWorkerProtocol.WorkIsDone) cmd).workerId;
            final String workId = ((MasterWorkerProtocol.WorkIsDone) cmd).workId;
            if (jobDatabase.isDone(workId)) {
                getSender().tell(new Ack(workId), getSelf());
            } else if (!jobDatabase.isInProgress(workId)) {
                log.info("Work {} not in progress, reported as done by worker {}", workId, workerId);
            } else {
                log.info("Work {} is done by worker {}", workId, workerId);
                changeWorkerToIdle(workerId, workId);
            }
        } else if (cmd instanceof MasterWorkerProtocol.WorkFailed) {
            final String workId = ((MasterWorkerProtocol.WorkFailed) cmd).workId;
            final String workerId = ((MasterWorkerProtocol.WorkFailed) cmd).workerId;
            if (jobDatabase.isInProgress(workId)) {
                log.info("Work {} failed by worker {}", workId, workerId);
                changeWorkerToIdle(workerId, workId);
            }
        } else if (cmd instanceof ServerInfo) {
                log.info("Accepted Server info request: {}", cmd);
                getSender().tell(cmd, getSelf());
        }
        else if (cmd instanceof Job) {
            final String workId = ((Job) cmd).jobId;
            // idempotent
            if (jobDatabase.isAccepted(workId)) {
                getSender().tell(new Ack(workId), getSelf());
            } else {
                log.info("Accepted work: {}", workId);
            }
        } else if (cmd==CleanupTick) {
            Iterator<Map.Entry<String, WorkerState>> iterator = workers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, WorkerState> entry = iterator.next();
                String workerId = entry.getKey();
                WorkerState state = entry.getValue();
                if (state.status.isBusy()) {
                    if (state.status.getDeadLine().isOverdue()) {
                        log.info("Work timed out: {}", state.status.getWorkId());
                        workers.remove(workerId);
                    }
                }
            }
        } else {
            unhandled(cmd);
        }
    }

    private void changeWorkerToIdle(String workerId, String workId) {
        if (workers.get(workerId).status.isBusy()) {
            workers.put(workerId, workers.get(workerId).copyWithStatus(new Idle()));
        }
    }
}
