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

package com.walmart.gatling.repository;

import java.io.UnsupportedEncodingException;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by walmart on 3/4/16.
 */
@XmlRootElement
public class WorkerModel {
    private String status;
    private String host;
    private String actor;
    private String workerId;
    private String role;

    public WorkerModel(String status, String actor, String workerId)  {
        this.status = status;
        try {
            this.actor = java.net.URLDecoder.decode(actor, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.workerId = workerId;
        this.host = this.actor.split("@")[1].split(":")[0];
        this.role = this.actor.split("/")[4].split("#")[0].replaceAll("[0-9]","");
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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
