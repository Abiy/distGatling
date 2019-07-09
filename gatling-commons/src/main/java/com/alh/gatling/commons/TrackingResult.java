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

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 */
@XmlRootElement
public class TrackingResult {

    private final long pendingCount;
    private final long inProgressCount;
    private boolean cancelled;
    private List<TaskTrackingInfo> Completed;
    private List<TaskTrackingInfo> Failed;
    public TrackingResult(long pendingCount, long inProgressCount) {
        this.pendingCount = pendingCount;
        this.inProgressCount = inProgressCount;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
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
