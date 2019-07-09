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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *  on 4/28/17.
 */
public class JobSummary implements Serializable {

    private List<TaskEvent> taskInfoList;
    private JobInfo jobInfo;

    public JobSummary() {
    }

    public JobSummary(JobInfo jobInfo) {
        taskInfoList = new ArrayList<>(jobInfo.count);
        this.jobInfo = jobInfo;
    }

    public List<TaskEvent> getTaskInfoList() {
        return taskInfoList;
    }

    public void setTaskInfoList(List<TaskEvent> taskInfoList) {
        this.taskInfoList = taskInfoList;
    }

    public JobInfo getJobInfo() {
        return jobInfo;
    }

    public void setJobInfo(JobInfo jobInfo) {
        this.jobInfo = jobInfo;
    }

    public void addTask(TaskEvent taskInfo) {
        getTaskInfoList().add(taskInfo);
    }

    public void addTask(List<TaskEvent> taskInfoList) {
        getTaskInfoList().addAll(taskInfoList);
    }

    public long getStartTime() {
        Optional<TaskEvent> task = getTaskInfoList().stream().min((t1, t2) -> Long.valueOf(t1.getStartTimeStamp()).compareTo(Long.valueOf(t2.getStartTimeStamp())));
        if (task.isPresent())
            return task.get().getStartTimeStamp();
        return 0l;
    }

    public long getEndTime() {
        Optional<TaskEvent> task = getTaskInfoList().stream().max((t1, t2) -> Long.valueOf(t1.getEndTimeStamp()).compareTo(Long.valueOf(t2.getEndTimeStamp())));
        if (task.isPresent())
            return task.get().getEndTimeStamp();
        return 0l;
    }

    public String getStatus() {
        if (getTaskInfoList().stream().allMatch(p -> p.getStatus().equalsIgnoreCase(JobState.JobStatusString.COMPLETED)))
            return JobState.JobStatusString.COMPLETED;

        if (getTaskInfoList().stream().anyMatch(p -> p.getStatus().equalsIgnoreCase(JobState.JobStatusString.FAILED)))
            return JobState.JobStatusString.FAILED;

        if (getTaskInfoList().stream().anyMatch(p -> p.getStatus().equalsIgnoreCase(JobState.JobStatusString.PENDING)))
            return JobState.JobStatusString.PENDING;

        if (getTaskInfoList().stream().anyMatch(p -> p.getStatus().equalsIgnoreCase(JobState.JobStatusString.TIMEDOUT)))
            return JobState.JobStatusString.TIMEDOUT;

        if (getTaskInfoList().stream().anyMatch(p -> p.getStatus().equalsIgnoreCase(JobState.JobStatusString.STARTED)))
            return JobState.JobStatusString.STARTED;

        if (getTaskInfoList().stream().allMatch(p -> p.getStatus().equalsIgnoreCase(JobState.JobStatusString.ACCEPTED)))
            return JobState.JobStatusString.ACCEPTED;

        return JobState.JobStatusString.FAILED;

    }

    public boolean runningJob() {
        String status = getStatus();
        return status.equalsIgnoreCase(JobState.JobStatusString.STARTED) || status.equalsIgnoreCase(JobState.JobStatusString.ACCEPTED) ||
                status.equalsIgnoreCase(JobState.JobStatusString.PENDING);
    }

    public boolean completedJob() {
        return getStatus().equalsIgnoreCase(JobState.JobStatusString.COMPLETED);
    }

    public boolean failedJob() {
        return getStatus().equalsIgnoreCase(JobState.JobStatusString.FAILED);
    }

    public boolean containsWork(String workId) {
        return getTaskInfoList().stream().anyMatch(p -> p.getTaskJobId().equalsIgnoreCase(workId));
    }


    public Optional<TaskEvent> getByWork(String workId) {
        return getTaskInfoList().stream().filter(p -> p.getTaskJobId().equalsIgnoreCase(workId)).findFirst();
    }


    public static final class JobInfo implements Serializable {
        public String partitionAccessKey;
        public String user;
        public String partitionName;
        public String jobName;
        public String trackingId;
        public short count;
        public String fileFullName;
        public boolean hasDataFeed;
        public boolean hasResourcesFeed;
        public String parameterString;
        public String dataFileName;
        public String resourcesFileName;
        public String jarFileName;

        public String getFileNameFromPackageName(){
            return fileFullName.replace('.', '/') + ".scala";
        }
        public JobInfo() {
        }

        public JobInfo(String partitionAccessKey, String user, String partitionName, String jobName, String trackingId, short count, String fileFullName, boolean hasResourcesFeed, String parameterString, String resourcesFilename
        , String jarFileName) {
            this.partitionAccessKey = partitionAccessKey;
            this.user = user;
            this.partitionName = partitionName;
            this.jobName = jobName;
            this.trackingId = trackingId;
            this.count = count;
            this.fileFullName = fileFullName;
            this.hasResourcesFeed = hasResourcesFeed;
            this.parameterString = parameterString;
            this.resourcesFileName = resourcesFilename;
            this.jarFileName =   jarFileName;
        }

        private JobInfo(Builder builder) {
            partitionAccessKey = builder.partitionAccessKey;
            user = builder.user;
            partitionName = builder.partitionName;
            jobName = builder.jobName;
            trackingId = builder.trackingId;
            count = builder.count;
            fileFullName = builder.fileFullName;
            this.hasResourcesFeed = builder.hasResourcesFeed;
            this.parameterString = builder.parameterString;
            this.resourcesFileName = builder.resourcesFileName;
            this.jarFileName = builder.jarFileName;
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public static final class Builder {
            private String partitionAccessKey;
            private String user;
            private String partitionName;
            private String jobName;
            private String trackingId;
            private short count;
            private String fileFullName;
            private boolean hasResourcesFeed;
            private String parameterString;
            public String resourcesFileName;
            public String jarFileName;

            private Builder() {
            }

            public Builder withPartitionAccessKey(String partitionAccessKey) {
                this.partitionAccessKey = partitionAccessKey;
                return this;
            }
            public Builder withFileFullName(String fileFullName) {
                this.fileFullName = fileFullName;
                return this;
            }

            public Builder withUser(String user) {
                this.user = user;
                return this;
            }

            public Builder withPartitionName(String partitionName) {
                this.partitionName = partitionName;
                return this;
            }

            public Builder withJobName(String jobName) {
                this.jobName = jobName;
                return this;
            }

            public Builder withTrackingId(String trackingId) {
                this.trackingId = trackingId;
                return this;
            }

            public Builder withHasResourcesFeed(boolean hasResourcesFeed) {
            	this.hasResourcesFeed = hasResourcesFeed;
                return this;
            }

            public Builder withParameterString(String parameterString) {
                this.parameterString = parameterString;
                return this;
            }
            public Builder withCount(short count) {
                this.count = count;
                return this;
            }

            public Builder withResourcesFileName(String resourcesFileName) {
                this.resourcesFileName = resourcesFileName;
                return this;
            }

            //
            public JobInfo build() {
                return new JobInfo(this);
            }

            public Builder withJarFileName(String jarFileName) {
                this.jarFileName = jarFileName;
                return this;
            }
        }
    }


}
