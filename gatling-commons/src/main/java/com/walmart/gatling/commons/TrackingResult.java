package com.walmart.gatling.commons;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by walmart
 */
@XmlRootElement
public class TrackingResult {

    private final long pendingCount;
    private final long inProgressCount;
    private List<TaskTrackingInfo> Completed;
    private List<TaskTrackingInfo> Failed;

    public TrackingResult(long pendingCount, long inProgressCount) {
        this.pendingCount = pendingCount;
        this.inProgressCount = inProgressCount;
    }

    public long getPendingCount() {
        return pendingCount;
    }

    public long getInProgressCount() {
        return inProgressCount;
    }

    public List<TaskTrackingInfo> getCompleted() {
        return Completed;
    }

    public void setCompleted(List<TaskTrackingInfo> completed) {
        Completed = completed;
    }

    public List<TaskTrackingInfo> getFailed() {
        return Failed;
    }

    public void setFailed(List<TaskTrackingInfo> failed) {
        Failed = failed;
    }

    @Override
    public String toString() {
        return "TrackingResult{" +
                "pendingCount=" + pendingCount +
                ", inProgressCount=" + inProgressCount +
                ", Completed=" + Completed +
                ", Failed=" + Failed +
                '}';
    }
}
