package com.walmart.gatling.commons;

import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by ahailemichael on 8/18/15.
 */
@XmlRootElement
public class AgentConfig {

    private Actor actor;
    private Job job;
    private LogServer logServer;
    private String contactPoint;

    public Stream<String> getContactPoint() {
        String[] contacts = contactPoint.split(",") ;
        return Stream.of(contacts)
                .map(p -> String.format("akka.tcp://%s@%s/system/receptionist",Constants.PerformanceSystem, p));
    }

    public void setContactPoint(String contactPoint) {
        this.contactPoint = contactPoint;
    }

    public LogServer getLogServer() {
        return logServer;
    }

    public void setLogServer(LogServer logServer) {
        this.logServer = logServer;
    }

    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public String getUrl(String filePath) {
        return getGenericUrl("api/log/stream","filePath",filePath);
    }

    public String getMasterUrl(String filePath) {
        return getGenericUrl("gatling/lib/stream","filePath",filePath);
    }

    public String getAbortUrl() {
        return getGenericUrl("gatling/server/abort","trackingId",StringUtils.EMPTY);
    }

    public String getGenericUrl(String path,String queryStringKey,String queryStringValue) {
        String host = logServer.getHostName();
        if (StringUtils.isEmpty(logServer.getHostName())) {
            try {
                host = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                ;
            }
        }
        String result = StringUtils.EMPTY;
        final String ENCODING = "UTF-8";
        try {
            result = String.format("http://%s:%s/%s?%s=%s", host, Integer.toString(logServer.getPort()),path, queryStringKey,URLEncoder.encode(queryStringValue, ENCODING));
        } catch (UnsupportedEncodingException e) {
            ;
        }

        return result;
    }

    @XmlRootElement
    public static class LogServer {
        private int port;
        private String hostName;

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

    }

    @XmlRootElement
    public static class Actor {
        private int numberOfActors;
        private int port;
        private String role;
        private String executerType;

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

        public String getExecuterType() {
            return executerType;
        }

        public void setExecuterType(String executerType) {
            this.executerType = executerType;
        }
    }

    @XmlRootElement
    public static class Job {
        private String path;
        private String logDirectory;
        private String command;
        private String artifact;
        private String mainClass;
        private String cpOrJar;
        private int[] exitValues;

        public String getLogDirectory() {
            return logDirectory;
        }

        public void setLogDirectory(String logDirectory) {
            this.logDirectory = logDirectory;
        }

        public String getArtifact() {
            return artifact;
        }

        public void setArtifact(String artifact) {
            this.artifact = artifact;
        }

        public String getJobArtifact(String name) {

            return MessageFormat.format(artifact, name);
        }

        public String getPath() {
            return this.path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getOutPath(String jobName, String jobId) {
            return String.format("%slogs/std/%s/%s.log", logDirectory, jobName, jobId);
        }

        public String getErrorPath(String jobName, String jobId) {
            return String.format("%slogs/errors/%s/%s.log", logDirectory, jobName, jobId);
        }

        public String getResultPath(String roleId,String jobId) {
            return String.format("%s/%s/%s", logDirectory, roleId, jobId);
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public String getMainClass() {
            return mainClass;
        }

        public void setMainClass(String mainClass) {
            this.mainClass = mainClass;
        }

        public String getCpOrJar() {
            return cpOrJar;
        }

        public void setCpOrJar(String cpOrJar) {
            this.cpOrJar = cpOrJar;
        }

        public int[] getExitValues() {
            if (exitValues==null)
                return new int[]{0};
            return exitValues;
        }

        public void setExitValues(int[] exitValues) {
            this.exitValues = exitValues;
        }

    }
}
