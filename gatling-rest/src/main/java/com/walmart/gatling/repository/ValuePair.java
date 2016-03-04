package com.walmart.gatling.repository;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by ahailem on 3/4/16.
 */
@XmlRootElement
public class ValuePair{
    private String status;
    private String host;

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    private String workerId;

    public ValuePair(String status, String host, String workerId) {
        this.status = status;
        this.host = host;
        this.workerId = workerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
