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

        public WorkFailed(String workerId, String workId) {
            this.workerId = workerId;
            this.workId = workId;
        }

        @Override
        public String toString() {
            return "WorkFailed{" +
                    "workerId='" + workerId + '\'' +
                    ", jobId='" + workId + '\'' +
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