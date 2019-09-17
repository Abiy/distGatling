package com.alh.gatling.commons;

import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.client.ClusterClientReceptionist;
import akka.persistence.Recovery;
import scala.collection.JavaConversions;
import scala.concurrent.duration.FiniteDuration;

import java.util.List;

/**
 * Master for not running on Kubernetes
 */
public class GenericMaster extends Master {

    public GenericMaster(FiniteDuration workTimeout, AgentConfig agentConfig) {
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
        return Props.create(GenericMaster.class, workTimeout, agentConfig);
    }

    @Override
    public void postStop() {
        cleanupTask.cancel();
    }

    private void notifyWorkers() {
        if (jobDatabase.hasJob()) {
            // could pick a few random instead of all
            for (WorkerState state : workers.values()) {
                if (state.status.isIdle()) {
                    state.ref.tell(MasterWorkerProtocol.WorkIsReady.getInstance(), getSelf());
                }
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
        return "generic-master";

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
            .matchEquals(CleanupTick, cmd -> onCleanupTick())
            .matchAny(cmd -> unhandled(cmd))
            .build();
    }


    protected void onCheckRunningOnKubernetes() {
        getSender().tell(new KubernetesMaster.RunningOnKubernetes(false), getSelf());
    }

    protected void onJob(Job cmd) {
        final String workId = cmd.jobId;
        // idempotent
        if (jobDatabase.isAccepted(workId)) {
            getSender().tell(new Ack(workId), getSelf());
        } else {
            log.info("Accepted work for workId={}", workId);
            persist(new JobState.JobAccepted(cmd), event -> {
                // Ack back to original sender
                getSender().tell(new Ack(event.job.jobId), getSelf());
                jobDatabase = jobDatabase.updated(event);
                notifyWorkers();
            });
        }
    }

    protected void onReport(Object cmd) {
        log.info("Accepted report request: {}", cmd);
        List<Worker.Result> result = jobDatabase.getCompletedResults(((Report) cmd).trackingId);
        reportExecutor.forward(new GenerateReport((Report) cmd, result), getContext());
    }

    protected void onWorkFailed(MasterWorkerProtocol.WorkFailed cmd) {
        final String workId = cmd.workId;
        final String workerId = cmd.workerId;
        log.warning("Work with workId={} failed by worker with workerId={}", workId, workerId);
        if (jobDatabase.isInProgress(workId)) {
            changeWorkerToIdle(workerId);
            persist(new JobState.JobFailed(workId, cmd.result), event -> {
                jobDatabase = jobDatabase.updated(event);
                notifyWorkers();
            });
        }
    }

    protected void onWorkIsDone(MasterWorkerProtocol.WorkIsDone cmd) {
        MasterWorkerProtocol.WorkIsDone workDone = cmd;
        final String workerId = workDone.workerId;
        final String workId = workDone.workId;
        if (jobDatabase.isDone(workId)) {
            getSender().tell(new Ack(workId), getSelf());
        } else if (!jobDatabase.isInProgress(workId)) {
            log.warning("Work with workId={} not in progress, reported as done by worker with workerId={}", workId, workerId);
        } else {
            log.info("Work with workId={} is done by worker with workerId={}", workId, workerId);
            changeWorkerToIdle(workerId);
            persist(new JobState.JobCompleted(workId, cmd.result), event -> {
                jobDatabase = jobDatabase.updated(event);
                getSender().tell(new Ack(event.workId), getSelf());
            });
        }
    }

    protected void onWorkerRequestsWork(MasterWorkerProtocol.WorkerRequestsWork cmd) {
        log.info("Worker requested workId={}", cmd);
        MasterWorkerProtocol.WorkerRequestsWork workReqMsg = cmd;
        final String workerId = workReqMsg.workerId;
        if (jobDatabase.hasJob()) {
            final WorkerState state = workers.get(workerId);
            if (state != null && state.status.isIdle()) {
                final Job job = jobDatabase.nextJob();//nextJob for the partition/role
                boolean jobWorkerRoleMatched = workReqMsg.role.equalsIgnoreCase(job.roleId);
                if (jobWorkerRoleMatched) {
                    persist(new JobState.JobStarted(job.jobId, workerId), event -> {
                        jobDatabase = jobDatabase.updated(event);
                        log.info("Giving worker with workerId={} some taskEvent with workId={}", workerId, event.workId);
                        workers.put(workerId, state.copyWithStatus(new Busy(event.workId, workTimeout.fromNow())));
                        getSender().tell(job, getSelf());
                    });
                } else {
                    persist(new JobState.JobPostponed(job.jobId), event -> {
                        jobDatabase = jobDatabase.updated(event);
                        log.info("Postponing work for workerId={}", workerId);
                    });
                    extendIdleExpiryTime(workerId);
                }
            }
        } else {
            extendIdleExpiryTime(workerId);
        }
    }

    protected void onRegisterWorker(MasterWorkerProtocol.RegisterWorker cmd) {
        String workerId = cmd.workerId;
        if (workers.containsKey(workerId)) {
            workers.put(workerId, workers.get(workerId).copyWithRef(getSender()));
        } else {
            log.info("Worker registered with workerId={}", workerId);
            workers.put(workerId, new WorkerState(getSender(), new Idle(workTimeout.fromNow())));
            if (jobDatabase.hasJob()) {
                getSender().tell(MasterWorkerProtocol.WorkIsReady.getInstance(), getSelf());
            }
        }
    }

}