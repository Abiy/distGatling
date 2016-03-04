package com.walmart.gatling.commons;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by walmart.
 */
@XmlRootElement
public class TaskTrackingInfo {
    public TaskTrackingInfo(String errorLog, String stdLog) {
        this.errorLog = errorLog;
        this.stdLog = stdLog;
    }

    private String errorLog;

    public String getStdLog() {
        return stdLog;
    }

    public void setStdLog(String stdLog) {
        this.stdLog = stdLog;
    }

    public String getErrorLog() {
        return errorLog;
    }

    public void setErrorLog(String errorLog) {
        this.errorLog = errorLog;
    }

    private String stdLog;

    @Override
    public String toString() {
        return "TaskTrackingInfo{" +
                "errorLog='" + errorLog + '\'' +
                ", stdLog='" + stdLog + '\'' +
                '}';
    }
}
