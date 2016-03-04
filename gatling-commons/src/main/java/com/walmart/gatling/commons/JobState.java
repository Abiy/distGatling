package com.walmart.gatling.commons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;


public final class JobState {

    private final Map<String, Master.Job> jobsInProgress;
    private final Set<String> acceptedJobIds;
    private final Set<String> doneJobIds;
    private final ConcurrentLinkedDeque<Master.Job> pendingJobs;
    private final  List<Worker.Result> failedJobs;
    private final List<Worker.Result> completedJobs;


    public JobState updated(JobDomainEvent event) {
        JobState newState = null;
        if (event instanceof JobAccepted) {
            return new JobState(this, (JobAccepted) event);
        } else if (event instanceof JobStarted) {
            return new JobState(this, (JobStarted) event);
        } else if (event instanceof JobCompleted) {
            return new JobState(this, (JobCompleted) event);
        } else if (event instanceof JobFailed) {
            return new JobState(this, (JobFailed) event);
        } else if (event instanceof JobTimedOut) {
            return new JobState(this, (JobTimedOut) event);
        } else if (event instanceof JobPostponed) {
            return new JobState(this, (JobPostponed) event);
        }
        return newState;

    }


    public JobState() {
        jobsInProgress = new HashMap<>();
        acceptedJobIds = new HashSet<>();
        doneJobIds = new HashSet<>();
        pendingJobs = new ConcurrentLinkedDeque<>();
        failedJobs = new ArrayList<>();
        completedJobs = new ArrayList<>();
    }

    private JobState(JobState jobState, JobAccepted workAccepted) {
        ConcurrentLinkedDeque<Master.Job> tmp_pendingJob = new ConcurrentLinkedDeque<Master.Job>(jobState.pendingJobs);
        Set<String> tmp_acceptedWorkIds = new HashSet<String>(jobState.acceptedJobIds);
        tmp_pendingJob.addLast(workAccepted.job);
        tmp_acceptedWorkIds.add(workAccepted.job.jobId);
        jobsInProgress = new HashMap<>(jobState.jobsInProgress);
        acceptedJobIds = tmp_acceptedWorkIds;
        doneJobIds = new HashSet<String>(jobState.doneJobIds);
        pendingJobs = tmp_pendingJob;
        failedJobs = new ArrayList<>(jobState.failedJobs);
        completedJobs = new ArrayList<>(jobState.completedJobs);

    }

    public JobState(JobState jobState, JobStarted workStarted) {
        ConcurrentLinkedDeque<Master.Job> tmp_pendingJob = new ConcurrentLinkedDeque<Master.Job>(jobState.pendingJobs);
        Map<String, Master.Job> tmp_workInProgress = new HashMap<String, Master.Job>(jobState.jobsInProgress);

        Master.Job job = tmp_pendingJob.removeFirst();
        if (!job.jobId.equals(workStarted.workId)) {
            throw new IllegalArgumentException("WorkStarted expected jobId " + job.jobId + "==" + workStarted.workId);
        }
        tmp_workInProgress.put(job.jobId, job);

        jobsInProgress = tmp_workInProgress;
        acceptedJobIds = new HashSet<String>(jobState.acceptedJobIds);
        ;
        doneJobIds = new HashSet<String>(jobState.doneJobIds);
        pendingJobs = tmp_pendingJob;
        failedJobs = new ArrayList<>(jobState.failedJobs);
        completedJobs = new ArrayList<>(jobState.completedJobs);
    }

    public JobState(JobState jobState, JobCompleted workCompleted) {
        Map<String, Master.Job> tmp_workInProgress = new HashMap<String, Master.Job>(jobState.jobsInProgress);
        Set<String> tmp_doneWorkIds = new HashSet<String>(jobState.doneJobIds);
        tmp_workInProgress.remove(workCompleted.workId);
        tmp_doneWorkIds.add(workCompleted.workId);
        jobsInProgress = tmp_workInProgress;
        acceptedJobIds = new HashSet<String>(jobState.acceptedJobIds);

        doneJobIds = tmp_doneWorkIds;
        pendingJobs = new ConcurrentLinkedDeque<Master.Job>(jobState.pendingJobs);
        failedJobs = new ArrayList<>(jobState.failedJobs);

        List<Worker.Result> tmp_completed = new ArrayList<>(jobState.completedJobs);
        Worker.Result result = (Worker.Result) workCompleted.result;
        tmp_completed.add(result);
        completedJobs = new ArrayList<>(tmp_completed);
    }


    public JobState(JobState jobState, JobFailed jobFailed) {
        Map<String, Master.Job> tmp_workInProgress = new HashMap<String, Master.Job>(jobState.jobsInProgress);
        ConcurrentLinkedDeque<Master.Job> tmp_pendingJob = new ConcurrentLinkedDeque<Master.Job>(jobState.pendingJobs);
        Set<String> acceptedJobIds_tmp =  new HashSet<>(jobState.acceptedJobIds) ;
        acceptedJobIds_tmp.remove(jobFailed.workId);
        //tmp_pendingJob.addLast(jobState.jobsInProgress.get(failedJobs.workId));
        tmp_workInProgress.remove(jobFailed.workId);
        jobsInProgress = tmp_workInProgress;
        acceptedJobIds = acceptedJobIds_tmp;
        doneJobIds = new HashSet<String>(jobState.doneJobIds);
        pendingJobs = tmp_pendingJob;

        completedJobs = new ArrayList<>(jobState.completedJobs);

        List<Worker.Result> tmp_Failed = new ArrayList<>(jobState.failedJobs);
        Worker.Result result = (Worker.Result) jobFailed.result;
        tmp_Failed.add(result);
        failedJobs = new ArrayList<>(tmp_Failed);
    }


    public JobState(JobState jobState, JobTimedOut jobTimedOut) {
        Map<String, Master.Job> tmp_workInProgress = new HashMap<String, Master.Job>(jobState.jobsInProgress);
        ConcurrentLinkedDeque<Master.Job> tmp_pendingJob = new ConcurrentLinkedDeque<Master.Job>(jobState.pendingJobs);
        tmp_pendingJob.addLast(jobState.jobsInProgress.get(jobTimedOut.workId));
        tmp_workInProgress.remove(jobTimedOut.workId);
        jobsInProgress = tmp_workInProgress;
        acceptedJobIds = new HashSet<String>(jobState.acceptedJobIds);
        doneJobIds = new HashSet<String>(jobState.doneJobIds);
        pendingJobs = tmp_pendingJob;
        failedJobs = new ArrayList<>(jobState.failedJobs);
        completedJobs = new ArrayList<>(jobState.completedJobs);
    }

    public JobState(JobState jobState, JobPostponed jobPostponed) {
        ConcurrentLinkedDeque<Master.Job> tmp_pendingJob = new ConcurrentLinkedDeque<Master.Job>(jobState.pendingJobs);
        Set<String> tmp_acceptedWorkIds = new HashSet<String>(jobState.acceptedJobIds);
        tmp_pendingJob.addLast(tmp_pendingJob.removeFirst());
        jobsInProgress = new HashMap<String, Master.Job>(jobState.jobsInProgress);
        acceptedJobIds = new HashSet<String>(jobState.acceptedJobIds);
        doneJobIds = new HashSet<String>(jobState.doneJobIds);
        pendingJobs = tmp_pendingJob;
        failedJobs = new ArrayList<>(jobState.failedJobs);
        completedJobs = new ArrayList<>(jobState.completedJobs);
    }


    public String toString() {
        return "" + acceptedJobIds.size();
    }

    public Master.Job nextJob() {
        return pendingJobs.getFirst();
    }

    public boolean hasJob() {
        return !pendingJobs.isEmpty();
    }

    public boolean isAccepted(String workId) {
        return acceptedJobIds.contains(workId);
    }

    public boolean isInProgress(String workId) {
        return jobsInProgress.containsKey(workId);
    }

    public Master.Job getJobInProgress(String workId) {
        return jobsInProgress.get(workId);
    }

    public boolean isDone(String workId) {
        return doneJobIds.contains(workId);
    }

    public static final class JobAccepted implements JobDomainEvent, Serializable {
        final Master.Job job;

        public JobAccepted(Master.Job job) {
            this.job = job;
        }
        @Override
        public String toString() {
            return JobStatusString.ACCEPTED;
        }

    }

    public static final class JobStarted implements JobDomainEvent, Serializable {
        final String workId;

        public JobStarted(String workId) {
            this.workId = workId;
        }
        @Override
        public String toString() {
            return JobStatusString.STARTED;
        }

    }

    public static final class JobCompleted implements JobDomainEvent, Serializable {
        final String workId;
        public final Object result;

        public JobCompleted(String workId, Object result) {
            this.workId = workId;
            this.result = result;
        }
        @Override
        public String toString() {
            return JobStatusString.COMPLETED;
        }

    }

    public static final class JobFailed implements JobDomainEvent, Serializable {
        public final String workId;
        public final Object result;
        public JobFailed(String workId, Object result) {
            this.workId = workId;
            this.result = result;
        }
        @Override
        public String toString() {
            return JobStatusString.FAILED;
        }

    }

    public static final class JobTimedOut implements JobDomainEvent, Serializable {
        final String workId;

        public JobTimedOut(String workId) {
            this.workId = workId;
        }
        @Override
        public String toString() {
            return JobStatusString.TIMEDOUT;
        }

    }

    public static final class JobPostponed implements JobDomainEvent, Serializable {
        final String workId;

        public JobPostponed(String workId) {
            this.workId = workId;
        }


        @Override
        public String toString() {
            return JobStatusString.POSTPONED;
        }
    }

    public static final class JobPending implements JobDomainEvent, Serializable {

        @Override
        public String toString() {
            return JobStatusString.PENDING;
        }
    }

    public TrackingResult getTrackingInfo(String trackingId) {
        long pendingCount = pendingJobs.stream().filter(p -> p.trackingId.equalsIgnoreCase(trackingId)).count();
        long inprogressCount = jobsInProgress.values().stream().filter(p -> p.trackingId.equalsIgnoreCase(trackingId)).count();
        TrackingResult result = new TrackingResult(pendingCount, inprogressCount);
        result.setCompleted(
                completedJobs.stream()
                        .filter(c -> c.job.trackingId.equalsIgnoreCase(trackingId))
                        .map(p -> new TaskTrackingInfo(p.errPath, (p.stdPath)))
                        .collect(Collectors.toList()));
        result.setFailed(failedJobs.stream().filter(c -> c.job.trackingId.equalsIgnoreCase(trackingId))
                .map(p -> new TaskTrackingInfo(p.errPath, p.stdPath))
                .collect(Collectors.toList()));
        return result;
    }

    public List<Worker.Result> getCompletedResults(String trackingId) {
        return completedJobs.stream()
                .filter(c -> c.job.trackingId.equalsIgnoreCase(trackingId))
                .collect(Collectors.toList());
    }

    public interface JobStatusString  {
        String COMPLETED  = "COMPLETED" ;
        String STARTED = "STARTED";
        String ACCEPTED  = "ACCEPTED" ;
        String FAILED = "FAILED";
        String TIMEDOUT  = "TIMEDOUT" ;
        String POSTPONED = "POSTPONED";
        String PENDING = "PENDING";
    }


}
