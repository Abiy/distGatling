package com.walmart.gatling.commons;

import java.util.List;

import javafx.util.Pair;

public class TaskEventBuilder {
    private String roleName;
    private List<Pair<String,String>> parameters;
    private String instance;
    private String status;
    private long[] internalDependencies;
    private long[] internalSteps;
    private String jobInstanceId;
    private String jobName;
    private String outputPath;
    private String[] datasources;
    private long trackingStatusId;
    private List<TaskEvent> dependentEvents;
    private int retry;
    private int retryDelayInSeconds;


    public TaskEventBuilder setRetryDelayInSeconds(int retryDelayInSeconds) {
        this.retryDelayInSeconds = retryDelayInSeconds;
        return this;
    }

    public TaskEventBuilder setRoleName(String roleName) {
        this.roleName = roleName;
        return this;
    }

    public TaskEventBuilder setParameters(List<Pair<String,String>> parameters) {
        this.parameters = parameters;
        return this;
    }

    public TaskEventBuilder setInstance(String instance) {
        this.instance = instance;
        return this;
    }

    public TaskEventBuilder setStatus(String status) {
        this.status = status;
        return this;
    }

    public TaskEventBuilder setInternalDependencies(long[] internalDependencies) {
        this.internalDependencies = internalDependencies;
        return this;
    }

    public TaskEventBuilder setInternalSteps(long[] internalSteps) {
        this.internalSteps = internalSteps;
        return this;
    }

    public TaskEventBuilder setJobInstanceId(String jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
        return this;
    }

    public TaskEventBuilder setJobName(String jobName) {
        this.jobName = jobName;
        return this;
    }

    public TaskEventBuilder setOutputPath(String outputPath) {
        this.outputPath = outputPath;
        return this;
    }

    public TaskEventBuilder setDatasources(String[] datasources) {
        this.datasources = datasources;
        return this;
    }

    public TaskEventBuilder setTrackingStatusId(long trackingStatusId) {
        this.trackingStatusId = trackingStatusId;
        return this;
    }

    public TaskEventBuilder setDependentEvents(List<TaskEvent> dependentEvents) {
        this.dependentEvents = dependentEvents;
        return this;
    }

    public TaskEventBuilder setRetry(int retry) {
        this.retry = retry;
        return this;
    }

    public TaskEvent build() {
        return new TaskEvent(roleName, parameters, instance, status, internalDependencies, internalSteps, jobInstanceId, jobName, outputPath, datasources, trackingStatusId, dependentEvents,retry,retryDelayInSeconds);
    }



}