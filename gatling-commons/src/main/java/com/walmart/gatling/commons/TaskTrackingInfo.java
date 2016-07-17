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
