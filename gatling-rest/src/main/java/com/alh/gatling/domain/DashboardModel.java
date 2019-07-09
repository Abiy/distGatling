/*
 *
 *   Copyright 2016 alh Technology
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

package com.alh.gatling.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Contains information about the cluster as a whole to be used for informing the dashboards.
 */
@Setter
public class DashboardModel {
    private Map<String, Long> status;
    private Map<String, Long> partition;
    private Map<String, Long> host;
    private Map<String, Long> partitionStatus;

    public KeyValList getStatus() {
        return new KeyValList(status);
    }

    public KeyValList getPartition() {
        return new KeyValList(partition);
    }

    public KeyValList getHost() {
        return new KeyValList(host);
    }

    public KeyValList getPartitionStatus() {
        return new KeyValList(partitionStatus);
    }

    @Getter
    public static class KeyValList {
        private Object[] keys;
        private Object[] values;

        KeyValList(Map<String, Long> map) {
            keys = map.keySet().stream().toArray();
            values = map.values().stream().toArray();
        }
    }
}
