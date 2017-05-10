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

package com.walmart.gatling.endpoint.v1;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Created by walmart
 */
@XmlRootElement
public class SimulationJobModel implements Serializable {

    private String partitionAccessKey;
    private String user;
    /***
     * The unique partition name/id
     */
    private String roleId;
    private String simulation;
    private String jobId;
    private String tag;
    private short count;
    private String fileFullName;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public short getCount() {
        return count;
    }

    public void setCount(short count) {
        this.count = count;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public long getSubmittedTime() {
        return LocalDateTime.now().toInstant(ZoneOffset.UTC).getEpochSecond();
    }

    public String getSimulation() {
        return simulation;
    }

    public void setSimulation(String simulation) {
        this.simulation = simulation;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getPartitionAccessKey() {
        return partitionAccessKey;
    }

    public void setPartitionAccessKey(String partitionAccessKey) {
        this.partitionAccessKey = partitionAccessKey;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getFileFullName() {
        return fileFullName;
    }

    public void setFileFullName(String fileName) {
        this.fileFullName = fileName;
    }

    @Override
    public String toString() {
        return "SimulationJobModel{" +
                "partitionAccessKey='" + partitionAccessKey + '\'' +
                ", user='" + user + '\'' +
                ", roleId='" + roleId + '\'' +
                ", simulation='" + simulation + '\'' +
                ", jobId='" + jobId + '\'' +
                ", tag='" + tag + '\'' +
                ", count=" + count +
                ", fileName='" + fileFullName + '\'' +
                '}';
    }
}
