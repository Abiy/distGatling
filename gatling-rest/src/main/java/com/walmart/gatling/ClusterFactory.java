package com.walmart.gatling;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;


import java.util.concurrent.TimeUnit;

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
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import com.walmart.gatling.commons.AgentConfig;
import com.walmart.gatling.commons.Constants;
import com.walmart.gatling.commons.HostUtils;
import com.walmart.gatling.commons.Master;

public class ClusterFactory {


    public static ActorSystem startMaster(int port, String role, boolean isPrimary,AgentConfig agentConfig) {
        String ip = HostUtils.lookupIp();
        String seed = String.format("akka.cluster.seed-nodes=[\"akka.tcp://%s%s", Constants.PerformanceSystem, "@"+ HostUtils.lookupIp() +":2551\"]");
        Config conf = ConfigFactory.parseString("akka.cluster.roles=[" + role + "]").
                withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port)).
                withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname=" + ip)).
                withFallback(ConfigFactory.parseString(seed)).
                withFallback(ConfigFactory.load("application"));

        ActorSystem system = ActorSystem.create(Constants.PerformanceSystem, conf);
        ClusterFactory.getMaster("backend",isPrimary,system,agentConfig);
        return system;
    }

    public static ActorRef  getMaster(String role, boolean isPrimary, ActorSystem system, AgentConfig agentConfig) {
        String journalPath = String.format("akka.tcp://%s%s", Constants.PerformanceSystem, "@"+ HostUtils.lookupIp() +":2551/user/store");
        startupSharedJournal(system, isPrimary, ActorPath$.MODULE$.fromString(journalPath));
        FiniteDuration workTimeout = Duration.create(120, "seconds");
        final ClusterSingletonManagerSettings settings =
                ClusterSingletonManagerSettings.create(system).withRole(role);

        ActorRef ref = system.actorOf(
                ClusterSingletonManager.props(Master.props(workTimeout,agentConfig), PoisonPill.getInstance(), settings),
                "master");
        return ref;
    }

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
