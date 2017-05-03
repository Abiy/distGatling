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

package com.walmart.gatling.endpoint.v1;

import java.util.Map;

/**
 * Created by walmart on 5/3/17.
 */
public class DashboardModel {
    private Map<String, Long> status;
    private Map<String, Long> partition;
    private Map<String, Long> host;
    private Map<String, Long> partitionStatus;

    public KeyValList getStatus() {
        KeyValList keyValList = new KeyValList();
        keyValList.setKeys(status.keySet().stream().toArray());
        keyValList.setValues(status.values().stream().toArray());
        return keyValList;
    }

    public void setStatus(Map<String, Long> status) {
        this.status = status;
    }

    public KeyValList getPartition() {
        KeyValList keyValList = new KeyValList();
        keyValList.setKeys(partition.keySet().stream().toArray());
        keyValList.setValues(partition.values().stream().toArray());
        return keyValList;
    }

    public void setPartition(Map<String, Long> partition) {
        this.partition = partition;
    }

    public KeyValList getHost() {
        KeyValList keyValList = new KeyValList();
        keyValList.setKeys(host.keySet().stream().toArray());
        keyValList.setValues(host.values().stream().toArray());
        return keyValList;
    }

    public void setHost(Map<String, Long> host) {
        this.host = host;
    }

    public KeyValList getPartitionStatus() {
        KeyValList keyValList = new KeyValList();
        keyValList.setKeys(partitionStatus.keySet().stream().toArray());
        keyValList.setValues(partitionStatus.values().stream().toArray());
        return keyValList;
    }

    public void setPartitionStatus(Map<String, Long> partitionStatus) {
        this.partitionStatus = partitionStatus;
    }

    public static class KeyValList {
        private Object[] keys;
        private Object[] values;

        public KeyValList() {
        }

        public Object[] getValues() {
            return values;
        }

        public void setValues(Object[] values) {
            this.values = values;
        }

        public Object[] getKeys() {
            return keys;
        }

        public void setKeys(Object[] keys) {
            this.keys = keys;
        }
    }
}
