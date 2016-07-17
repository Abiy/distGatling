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

/**
 * Created by walmart
 */
@XmlRootElement
public class JobModel {

    private  String roleId;
    private String simulation;
    private String jobId;
    private short count;

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
}
