package com.walmart.gatling.repository;

import java.io.UnsupportedEncodingException;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by ahailem on 3/4/16.
 */
@XmlRootElement
public class ValuePair {
    private String status;
    private String host;
    private String actor;
    private String workerId;

    public ValuePair(String status, String actor, String workerId)  {
        this.status = status;
        try {
            this.actor = java.net.URLDecoder.decode(actor, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.workerId = workerId;
        this.host = this.actor.split("@")[1].split(":")[0];
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
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
