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

package com.walmart.gatling.domain;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * A model of the desired job to be run on the cluster.
 */
@Data
@XmlRootElement
public class SimulationJobModel implements Serializable {
    private String partitionAccessKey;
    private String user;
    private String roleId; // The unique partition name/id
    private String simulation;
    private String dataFile;
    private String jobId;
    private String tag;
    private short count;
    private String fileFullName;
    private String parameterString;

}
