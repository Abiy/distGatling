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

import java.io.Serializable;

public abstract class MasterWorkerProtocol {

    // Messages from/to Workers

    public static final class RegisterWorker implements Serializable {
        public final String workerId;

        public RegisterWorker(String workerId) {
            this.workerId = workerId;
        }

        @Override
        public String toString() {
            return "RegisterWorker{" +
                    "workerId='" + workerId + '\'' +
                    '}';
        }
    }

    public static final class WorkerRequestsWork implements Serializable {
        public final String workerId;
        public final String role;

        public WorkerRequestsWork(String workerId, String role) {
            this.workerId = workerId;
            this.role = role;
        }

        @Override
        public String toString() {
            return "WorkerRequestsWork{" +
                    "workerId='" + workerId + '\'' +
                    '}';
        }
    }

    public static final class WorkerRequestsFile implements Serializable {
        public final String workerId;
        public final String role;
        public final String host;

        public WorkerRequestsFile(String workerId, String role,String host) {
            this.workerId = workerId;
            this.role = role;
            this.host = host;
        }

        @Override
        public String toString() {
            return "WorkerRequestsFile{" +
                    "workerId='" + workerId + '\'' +
                    ", role='" + role + '\'' +
                    ", host='" + host + '\'' +
                    '}';
        }
    }

    public static final class WorkIsDone implements Serializable {
        public final String workerId;
        public final String workId;
        public final Object result;

        public WorkIsDone(String workerId, String workId, Object result) {
            this.workerId = workerId;
            this.workId = workId;
            this.result = result;
        }

        @Override
        public String toString() {
            return "WorkIsDone{" +
                    "workerId='" + workerId + '\'' +
                    ", jobId='" + workId + '\'' +
                    ", result=" + result +
                    '}';
        }
    }

    public static final class WorkFailed implements Serializable {
        public final String workerId;
        public final String workId;
        public final Object result;

        public WorkFailed(String workerId, String workId,Object result) {
            this.workerId = workerId;
            this.workId = workId;
            this.result = result;
        }

        @Override
        public String toString() {
            return "WorkFailed{" +
                    "workerId='" + workerId + '\'' +
                    ", workId='" + workId + '\'' +
                    ", result=" + result +
                    '}';
        }
    }


    public static final class WorkInProgress implements Serializable {
        public final String workerId;
        public final String workId;

        public WorkInProgress(String workerId, String workId) {
            this.workerId = workerId;
            this.workId = workId;
        }

        @Override
        public String toString() {
            return "WorkInProgress{" +
                    "workerId='" + workerId + '\'' +
                    ", jobId='" + workId + '\'' +
                    '}';
        }
    }
    // Messages to Workers

    public static final class WorkIsReady implements Serializable {
        private static final WorkIsReady instance = new WorkIsReady();

        public static WorkIsReady getInstance() {
            return instance;
        }
    }
}