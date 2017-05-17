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

public abstract class MasterClientProtocol {

    // Messages from/to command client

    public static final class CommandLineJob implements Serializable {
        public final String clientId;
        public final ClientConfig clientConfig;

        public CommandLineJob(String clientId, ClientConfig clientConfig) {
            this.clientId = clientId;
            this.clientConfig = clientConfig;
        }

        @Override
        public String toString() {
            return "CommandJob{" +
                    "clientId='" + clientId + '\'' +
                    ", clientConfig=" + clientConfig +
                    '}';
        }
    }

    public static final class CommandLineJobDone implements Serializable {

    }


    public static final class CommandLineJobAccepted implements Serializable {
        private Master.Job job;

        public CommandLineJobAccepted() {

        }

        public CommandLineJobAccepted(Master.Job job) {
            this.job = job;
        }

        @Override
        public String toString() {
            return "CommandLineJobAccepted{" +
                    "job=" + job +
                    '}';
        }
    }


}