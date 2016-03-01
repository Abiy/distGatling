package com.walmart.gatling.commons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.util.Pair;

/**
 * Created by ahailemichael on 7/22/15.
 */
public class TaskEvent implements Serializable {

    private String roleName;
    private List<Pair<String, String>> parameters;
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
    private String procesingId;

    public TaskEvent() {
        dependentEvents = new ArrayList<>();
    }

    public TaskEvent(String roleName, List<Pair<String, String>> parameters, String instance, String status,
                     long[] internalDependencies, long[] internalSteps, String jobInstanceId, String jobName,
                     String outputPath, String[] datasources, long trackingStatusId, List<TaskEvent> dependentEvents,
                     int retry, int retryDelayInSeconds) {
        this.roleName = roleName;
        this.parameters = parameters;
        this.instance = instance;
        this.status = status;
        this.internalDependencies = internalDependencies;
        this.internalSteps = internalSteps;
        this.jobInstanceId = jobInstanceId;
        this.jobName = jobName;
        this.outputPath = outputPath;
        this.datasources = datasources;
        this.trackingStatusId = trackingStatusId;
        this.dependentEvents = dependentEvents;
        this.retry = retry;
        this.retryDelayInSeconds = retryDelayInSeconds;
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

    public long[] getInternalDependencies() {
        return internalDependencies;
    }

    public void setInternalDependencies(long[] internalDependencies) {
        this.internalDependencies = internalDependencies;
    }

    public long[] getInternalSteps() {
        return internalSteps;
    }

    public void setInternalSteps(long[] internalSteps) {
        this.internalSteps = internalSteps;
    }

    public String getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(String jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }

    public String getJobName() {
        return jobName.toLowerCase();
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

    public String[] getDatasources() {
        return datasources;
    }

    public void setDatasources(String[] datasources) {
        this.datasources = datasources;
    }

    public long getTrackingStatusId() {
        return trackingStatusId;
    }

    public void setTrackingStatusId(long trackingStatusId) {
        this.trackingStatusId = trackingStatusId;
    }

    public List<TaskEvent> getDependentEvents() {
        return dependentEvents;
    }

    public void setDependentEvents(List<TaskEvent> dependentEvents) {
        this.dependentEvents = dependentEvents;
    }

    public void addDependentTask(TaskEvent event) {
        dependentEvents.add(event);
    }

    public String getParameterValue(String key) {
        Optional<Pair<String, String>> param
                = getParameters().stream().filter(p -> p.getKey().equalsIgnoreCase(key)).findFirst();
        return param.get().getValue();
    }

    public void setParameterValue(String key, String value) {
        List<Pair<String, String>> param = getParameters().stream().filter(p -> !p.getKey().equalsIgnoreCase(key)).collect(Collectors.toList());
        param.add(new Pair<>(key, value));
        parameters = param;
    }
}
