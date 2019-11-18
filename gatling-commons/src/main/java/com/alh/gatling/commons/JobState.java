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

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;


public final class JobState {

    private final Map<String, Master.Job> jobsInProgress;
    private final Set<String> acceptedJobIds;
    private final Set<String> doneJobIds;
    private final ConcurrentLinkedDeque<Master.Job> pendingJobs;
    private final ConcurrentLinkedQueue<Worker.Result> failedJobs;
    private final ConcurrentLinkedQueue<Worker.Result> completedJobs;
    private HashMap<String,JobSummary> jobSummary ;

    public JobState() {
        jobsInProgress = new HashMap<>();
        acceptedJobIds = new HashSet<>();
        doneJobIds = new HashSet<>();
        pendingJobs = new ConcurrentLinkedDeque<>();
        failedJobs = new ConcurrentLinkedQueue<>();
        completedJobs = new ConcurrentLinkedQueue<>();
        jobSummary = new HashMap<>();
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
        failedJobs = new ConcurrentLinkedQueue<>(jobState.failedJobs);
        completedJobs = new ConcurrentLinkedQueue<>(jobState.completedJobs);
        jobSummary = new HashMap<>(jobState.jobSummary);
        //job summary
        JobSummary summary = jobSummary.get(workAccepted.job.trackingId);
        TaskEvent taskInfo = (TaskEvent) workAccepted.job.taskEvent;
        if(summary == null){
            summary = new JobSummary(taskInfo.getJobInfo());
            jobSummary.put(workAccepted.job.trackingId, summary);
        }
        taskInfo.setStartTimeStamp(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        taskInfo.setTaskJobId(workAccepted.job.jobId);
        taskInfo.setStatus(JobStatusString.PENDING);
        summary.addTask(taskInfo);


    }


    public JobState(JobState jobState, JobStarted workStarted) {
        ConcurrentLinkedDeque<Master.Job> tmp_pendingJob = new ConcurrentLinkedDeque<>(jobState.pendingJobs);
        Map<String, Master.Job> tmp_workInProgress = new HashMap<>(jobState.jobsInProgress);

        Master.Job job = tmp_pendingJob.removeFirst();
        if (!job.jobId.equals(workStarted.workId)) {
            throw new IllegalArgumentException("WorkStarted expected jobId " + job.jobId + "==" + workStarted.workId);
        }
        tmp_workInProgress.put(job.jobId, job);

        jobsInProgress = tmp_workInProgress;
        acceptedJobIds = new HashSet<String>(jobState.acceptedJobIds);
        doneJobIds = new HashSet<String>(jobState.doneJobIds);
        pendingJobs = tmp_pendingJob;
        failedJobs = new ConcurrentLinkedQueue<>(jobState.failedJobs);
        completedJobs = new ConcurrentLinkedQueue<>(jobState.completedJobs);
        jobSummary = new HashMap<>(jobState.jobSummary);
        //job summary
        JobSummary summary = jobSummary.get(job.trackingId);
        Optional<TaskEvent> task = summary.getByWork(job.jobId);
        if (task.isPresent()) {
            task.get().setStatus(JobStatusString.STARTED);
            task.get().setWorkerId(workStarted.workerId);
        }
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
        failedJobs = new ConcurrentLinkedQueue<>(jobState.failedJobs);

        List<Worker.Result> tmp_completed = new ArrayList<>(jobState.completedJobs);
        Worker.Result result = (Worker.Result) workCompleted.result;
        tmp_completed.add(result);
        completedJobs = new ConcurrentLinkedQueue<>(tmp_completed);
        jobSummary = new HashMap<>(jobState.jobSummary);
        //job summary
        JobSummary summary = jobSummary.get(result.job.trackingId);
        Optional<TaskEvent> task = summary.getByWork(result.job.jobId);
        if (task.isPresent()) {
            task.get().setStatus(JobStatusString.COMPLETED);
            task.get().setEndTimeStamp(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            task.get().setErrorLogPath(result.errPath);
            task.get().setStdLogPath(result.stdPath);
        }
    }

    public JobState(JobState jobState, JobFailed jobFailed) {
        Map<String, Master.Job> tmp_workInProgress = new HashMap<String, Master.Job>(jobState.jobsInProgress);
        ConcurrentLinkedDeque<Master.Job> tmp_pendingJob = new ConcurrentLinkedDeque<Master.Job>(jobState.pendingJobs);
        Set<String> acceptedJobIds_tmp = new HashSet<>(jobState.acceptedJobIds);
        acceptedJobIds_tmp.remove(jobFailed.workId);
        //tmp_pendingJob.addLast(jobState.jobsInProgress.get(failedJobs.workId));
        tmp_workInProgress.remove(jobFailed.workId);
        jobsInProgress = tmp_workInProgress;
        acceptedJobIds = acceptedJobIds_tmp;
        doneJobIds = new HashSet<>(jobState.doneJobIds);
        pendingJobs = tmp_pendingJob;

        completedJobs = new ConcurrentLinkedQueue<>(jobState.completedJobs);

        List<Worker.Result> tmp_Failed = new ArrayList<>(jobState.failedJobs);
        Worker.Result result = (Worker.Result) jobFailed.result;
        tmp_Failed.add(result);
        failedJobs = new ConcurrentLinkedQueue<>(tmp_Failed);
        jobSummary = new HashMap<>(jobState.jobSummary);
        //job summary
        JobSummary summary = jobSummary.get(result.job.trackingId);
        Optional<TaskEvent> task = summary.getByWork(result.job.jobId);
        if (task.isPresent()) {
            task.get().setStatus(JobStatusString.FAILED);
            task.get().setEndTimeStamp(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            task.get().setErrorLogPath(result.errPath);
            task.get().setStdLogPath(result.stdPath);
        }
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
        failedJobs = new ConcurrentLinkedQueue<>(jobState.failedJobs);
        completedJobs = new ConcurrentLinkedQueue<>(jobState.completedJobs);
        jobSummary = new HashMap<>(jobState.jobSummary);
        //job summary
        Optional<JobSummary> summary = jobSummary.values().stream().filter(s -> s.containsWork(jobTimedOut.workId)).findFirst();
        if(summary.isPresent()) {
            Optional<TaskEvent> task = summary.get().getByWork(jobTimedOut.workId);
            if (task.isPresent()) {
                task.get().setStatus(JobStatusString.TIMEDOUT);
            }
        }
    }

    public JobState(JobState jobState, JobPostponed jobPostponed) {
        ConcurrentLinkedDeque<Master.Job> tmp_pendingJob = new ConcurrentLinkedDeque<Master.Job>(jobState.pendingJobs);
        Set<String> tmp_acceptedWorkIds = new HashSet<String>(jobState.acceptedJobIds);
        tmp_pendingJob.addLast(tmp_pendingJob.removeFirst());
        jobsInProgress = new HashMap<String, Master.Job>(jobState.jobsInProgress);
        acceptedJobIds = new HashSet<String>(jobState.acceptedJobIds);
        doneJobIds = new HashSet<String>(jobState.doneJobIds);
        pendingJobs = tmp_pendingJob;
        failedJobs = new ConcurrentLinkedQueue<>(jobState.failedJobs);
        completedJobs = new ConcurrentLinkedQueue<>(jobState.completedJobs);
        jobSummary = new HashMap<>(jobState.jobSummary);
        //job summary
        Optional<JobSummary> summary = jobSummary.values().stream().filter(s -> s.containsWork(jobPostponed.workId)).findFirst();
        if(summary.isPresent()) {
            Optional<TaskEvent> task = summary.get().getByWork(jobPostponed.workId);
            if (task.isPresent()) {
                task.get().setStatus(JobStatusString.POSTPONED);
            }
        }
    }

    public HashMap<String, JobSummary> getJobSummary() {
        return jobSummary;
    }

    public int getPendingJobsCount() {
        return pendingJobs.size();
    }

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

    public TrackingResult getTrackingInfo(String trackingId) {
        long pendingCount = pendingJobs.stream().filter(p -> p.trackingId.equalsIgnoreCase(trackingId)).count();
        long inprogressCount = jobsInProgress.values().stream().filter(p -> p.trackingId.equalsIgnoreCase(trackingId)).count();
        TrackingResult result = new TrackingResult(pendingCount, inprogressCount);
        //System.out.println("Completed Tracking: "+ completedJobs);
        result.setCompleted(
                completedJobs.stream()
                        .filter(c -> c.job.trackingId.equalsIgnoreCase(trackingId))
                        .map(p -> new TaskTrackingInfo(p.errPath, (p.stdPath)))
                        .collect(Collectors.toList()));
        //System.out.println("Failed Tracking: "+ failedJobs);
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

    public interface JobStatusString {
        String COMPLETED = "COMPLETED";
        String STARTED = "STARTED";
        String ACCEPTED = "ACCEPTED";
        String FAILED = "FAILED";
        String TIMEDOUT = "TIMEDOUT";
        String POSTPONED = "POSTPONED";
        String PENDING = "PENDING";
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
        final String workerId;

        public JobStarted(String workId,String workerId) {
            this.workId = workId;
            this.workerId = workerId;
        }

        @Override
        public String toString() {
            return JobStatusString.STARTED;
        }

    }

    public static final class JobCompleted implements JobDomainEvent, Serializable {
        public final Object result;
        final String workId;

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


}
