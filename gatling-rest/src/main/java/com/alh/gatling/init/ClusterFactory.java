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

package com.alh.gatling.init;

import akka.actor.ActorIdentity;
import akka.actor.ActorPath;
import akka.actor.ActorPath$;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Identify;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.persistence.journal.leveldb.SharedLeveldbJournal;
import akka.persistence.journal.leveldb.SharedLeveldbStore;
import akka.util.Timeout;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.alh.gatling.commons.AgentConfig;
import com.alh.gatling.commons.Constants;
import com.alh.gatling.commons.HostUtils;
import com.alh.gatling.commons.Master;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public class ClusterFactory {

    /**
     * Creates the actor system with the master
     * @param port
     * @param role
     * @param isPrimary
     * @param agentConfig
     * @return
     */
    public static ActorSystem startMaster(int port, String role, boolean isPrimary,AgentConfig agentConfig) {
        String ip = HostUtils.lookupIp();
        String seed = String.format("akka.cluster.seed-nodes=[\"akka.tcp://%s@%s:%s\"]", Constants.PerformanceSystem, ip ,port);
        Config conf = ConfigFactory.parseString("akka.cluster.roles=[" + role + "]").
                withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port)).
                withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname=" + ip)).
                withFallback(ConfigFactory.parseString(seed)).
                withFallback(ConfigFactory.load("application"));

        ActorSystem system = ActorSystem.create(Constants.PerformanceSystem, conf);
        ClusterFactory.getMaster(port,role,isPrimary,system,agentConfig,ip);
        return system;
    }

    /**
     * Creates the master actor using cluster singleton manager in the specified actor system
     * @param port
     * @param role
     * @param isPrimary
     * @param system
     * @param agentConfig
     * @param ip
     * @return
     */
    public static ActorRef  getMaster(int port,String role, boolean isPrimary, ActorSystem system, AgentConfig agentConfig,String ip) {
        String journalPath = String.format("akka.tcp://%s@%s:%s/user/store", Constants.PerformanceSystem,  ip ,port);
        startupSharedJournal(system, isPrimary, ActorPath$.MODULE$.fromString(journalPath));
        FiniteDuration workTimeout = Duration.create(120, "seconds");
        final ClusterSingletonManagerSettings settings =
                ClusterSingletonManagerSettings.create(system).withRole(role);

        ActorRef ref = system.actorOf(
                ClusterSingletonManager.props(Master.props(workTimeout,agentConfig), PoisonPill.getInstance(), settings),
                "master");
        return ref;
    }

    /**
     * Associates a journal actor for master, the master is a persistent actor
     * @param system
     * @param startStore
     * @param path
     */
    public static void startupSharedJournal(final ActorSystem system, boolean startStore, final ActorPath path) {
        // Start the shared journal on one node (don't crash this SPOF)
        // This will not be needed with a distributed journal
        if (startStore) {
            system.actorOf(Props.create(SharedLeveldbStore.class), "store");
        }
        // register the shared journal

        Timeout timeout = new Timeout(15, TimeUnit.SECONDS);
        ActorSelection actorSelection = system.actorSelection(path);
        Future<Object> f = Patterns.ask(actorSelection, new Identify(null), timeout);
        f.onSuccess(new OnSuccess<Object>() {

            @Override
            public void onSuccess(Object arg0) throws Throwable {
                if (arg0 instanceof ActorIdentity && ((ActorIdentity) arg0).getRef()!=null) {
                    SharedLeveldbJournal.setStore(((ActorIdentity) arg0).getRef(), system);
                } else {
                    System.err.println("Shared journal not started at " + path);
                    System.exit(-1);
                }

            }
        }, system.dispatcher());

        f.onFailure(new OnFailure() {
            public void onFailure(Throwable arg0) throws Throwable {
                System.err.println("Lookup of shared journal at " + path + " timed out");
            }
        }, system.dispatcher());
    }

}
