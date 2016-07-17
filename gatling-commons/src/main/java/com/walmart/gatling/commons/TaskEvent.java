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
import java.util.stream.Collectors;

import javafx.util.Pair;

/**
 * Created by walmart
 */
public class TaskEvent implements Serializable {

    private String roleName;
    private List<Pair<String, String>> parameters;
    private String instance;
    private String status;
    private String jobInstanceId;
    private String jobName;
    private String outputPath;
    private int retry;
    private int retryDelayInSeconds;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<Pair<String, String>> getParameters() {
        return parameters;
    }

    public void setParameters(List<Pair<String, String>> parameters) {
        this.parameters = parameters;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(String jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public int getRetryDelayInSeconds() {
        return retryDelayInSeconds;
    }

    public void setRetryDelayInSeconds(int retryDelayInSeconds) {
        this.retryDelayInSeconds = retryDelayInSeconds;
    }


}
