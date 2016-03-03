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
