package com.walmart.gatling;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.walmart.gatling.commons.AgentConfig;
import com.walmart.gatling.commons.Constants;
import com.walmart.gatling.commons.HostUtils;
import com.walmart.gatling.commons.ScriptExecutor;
import com.walmart.gatling.commons.WorkExecutor;
import com.walmart.gatling.commons.Worker;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import akka.actor.ActorPath;
import akka.actor.ActorPaths;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.client.ClusterClient;
import akka.cluster.client.ClusterClientSettings;

public class WorkerFactory {

    public static ActorSystem startWorkersWithExecutors(AgentConfig agent) {
        Config conf = ConfigFactory.parseString("akka.cluster.roles=[" + agent.getActor().getRole() + "]")
                .withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=" + agent.getActor().getPort()))
                .withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname=" + HostUtils.lookupIp()))
                .withFallback(ConfigFactory.load("application"));

        ActorSystem system = ActorSystem.create(Constants.PerformanceSystem, conf);

        Set<ActorPath> initialContacts = new HashSet<>(agent.getContactPoint()
                    .map(p->ActorPaths.fromString(p))
                    .collect(Collectors.toList()));

        ClusterClientSettings settings =  ClusterClientSettings.create(system).withInitialContacts(initialContacts);
        final ActorRef clusterClient = system.actorOf(ClusterClient.props(settings), "clusterClient");

        IntStream.range(1,agent.getActor().getNumberOfActors()+1).forEach(i->
            system.actorOf(Worker.props(clusterClient,
                            createWorkExecutor(agent),
                            agent.getActor().getRole()),
                            agent.getActor().getRole()+i)
        );
        return system;

    }

    private static Props createWorkExecutor(AgentConfig agentConfig){
       return Props.create(ScriptExecutor.class, agentConfig);
    }

}
