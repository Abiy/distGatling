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

package com.walmart.gatling.commons;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.stream.Stream;

/**
 * Created by walmart on 5/16/17.
 */

@XmlRootElement
public class ClientConfig implements Serializable {
    private int numberOfActors;
    private int port;
    private String role;
    private String jarPath;
    private String dataFeedPath;
    private boolean quite;
    private String className;
    private short parallelism;
    private String accessKey;
    private String partitionName;
    private String parameterString;
    private String contactPoint;
    private String userName;
    private String host;
    private boolean remoteArtifact;
    private String dataFeedFileName;

    public String getDataFeedFileName() {
        return dataFeedFileName;
    }

    public void setDataFeedFileName(String dataFeedFileName) {
        this.dataFeedFileName = dataFeedFileName;
    }

    public boolean isRemoteArtifact() {
        return remoteArtifact;
    }

    public void setRemoteArtifact(boolean remoteArtifact) {
        this.remoteArtifact = remoteArtifact;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public String getDataFeedPath() {
        return dataFeedPath;
    }

    public void setDataFeedPath(String dataFeedPath) {
        this.dataFeedPath = dataFeedPath;
    }

    public boolean isQuite() {
        return quite;
    }

    public void setQuite(boolean quite) {
        this.quite = quite;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public short getParallelism() {
        return parallelism;
    }

    public void setParallelism(short parallelism) {
        this.parallelism = parallelism;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public String getParameterString() {
        return parameterString;
    }

    public void setParameterString(String parameterString) {
        this.parameterString = parameterString;
    }

    public Stream<String> getContactPoint() {
        String[] contacts = contactPoint.split(",") ;
        return Stream.of(contacts)
                .map(p -> String.format("akka.tcp://%s@%s/system/receptionist", Constants.PerformanceSystem, p));
    }

    public void setContactPoint(String contactPoint) {
        this.contactPoint = contactPoint;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getNumberOfActors() {
        return numberOfActors;
    }

    public void setNumberOfActors(int numberOfActors) {
        this.numberOfActors = numberOfActors;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
