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

package com.alh.gatling.commons;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.client.ClusterClient;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.UUID;

public class CommandClientActor extends AbstractActor {

    private final ActorRef clusterClient;
    private final ClientConfig clientConfig;
    private final String clientId = UUID.randomUUID().toString();
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private String currentJobId = null;

    private final Procedure<Object> working = new Procedure<Object>() {
        public void apply(Object message) {
            log.info("{}", message);
            if (message instanceof MasterClientProtocol.CommandLineJobSubmitted) {
                log.info("Job submitted. tracking info =>" + ((MasterClientProtocol.CommandLineJobSubmitted) message).getTrackingDetail());
                System.exit(0);
            } else {
                unhandled(message);
            }
        }
    };

    private final Procedure<Object> idle = new Procedure<Object>() {
        public void apply(Object message) {
            if(message== StartCommand){
                //upload artifacts if not remoteArtifact
                //upload jar=>jarPath
                //upload feed=>feedPath
                //
                sendToMaster(new MasterClientProtocol.CommandLineJob(clientId, clientConfig));
                getContext().become(receiveBuilder()
                        .matchAny(p->working.apply(p))
                        .build());
            }
            else {
                unhandled(message);
            }
        }
    };

    private static final Object StartCommand = new Object() {
        @Override
        public String toString() {
            return "SubmitCommandToCluster";
        }
    };

    public CommandClientActor(ActorRef clusterClient, ClientConfig clientConfig) {
        this.clusterClient = clusterClient;
        this.clientConfig = clientConfig;
        FiniteDuration workTimeout = Duration.create(10, "seconds");
        getContext().system().scheduler().scheduleOnce(workTimeout, getSelf(), StartCommand, getContext().dispatcher(), getSelf());
    }

    public static Props props(ActorRef clusterClient, ClientConfig clientConfig) {
        return Props.create(CommandClientActor.class, clusterClient, clientConfig);
    }



    private String jobId() {
        if (currentJobId!=null)
            return currentJobId;
        else
            throw new IllegalStateException("Not working");
    }


    @Override
    public void postStop() {
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .build();
    }


    {
        getContext().become(receiveBuilder()
                .matchAny(p->idle.apply(p))
                .build());
    }

    @Override
    public void unhandled(Object message) {

        if (message instanceof MasterWorkerProtocol.WorkIsReady) {
            // do nothing
        } else {
            super.unhandled(message);
        }
    }

    private void sendToMaster(Object msg) {
        clusterClient.tell(new ClusterClient.SendToAll("/user/master/singleton", msg), getSelf());
    }


}
