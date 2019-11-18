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
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TaskEvent implements Serializable {

    private List<String> parameters;
    private String jobName;
    private long startTimeStamp;
    private long endTimeStamp;
    private String workerId;
    private String errorLogPath;
    private String stdLogPath;
    private String status;//Master.JobStatusString
    private String taskJobId;
    private JobSummary.JobInfo jobInfo;

    public TaskEvent() {
        parameters = new ArrayList<>();
    }

    public String getTaskJobId() {
        return taskJobId;
    }

    public void setTaskJobId(String taskJobId) {
        this.taskJobId = taskJobId;
    }

    public JobSummary.JobInfo getJobInfo() {
        return jobInfo;
    }

    public void setJobInfo(JobSummary.JobInfo jobInfo) {
        this.jobInfo = jobInfo;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public void setStartTimeStamp(long startTimeStamp) {
        this.startTimeStamp = startTimeStamp;
    }

    public long getEndTimeStamp() {
        return endTimeStamp;
    }

    public void setEndTimeStamp(long endTimeStamp) {
        this.endTimeStamp = endTimeStamp;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getErrorLogPath() {
        return errorLogPath;
    }

    public void setErrorLogPath(String errorLogPath) {
        this.errorLogPath = errorLogPath;
    }

    public String getStdLogPath() {
        return stdLogPath;
    }

    public void setStdLogPath(String stdLogPath) {
        this.stdLogPath = stdLogPath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
