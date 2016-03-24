package com.walmart.gatling;

import com.walmart.gatling.commons.AgentConfig;
import com.walmart.gatling.commons.HostUtils;
import com.walmart.gatling.commons.MasterClientActor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.RoundRobinPool;

/**
 * Created by walmart.
 */
@Configuration
public class SystemConfiguration {

    @Value("${actor.numberOfActors}")
    private int numberOfActors;
    @Value("${actor.port}")
    private int port;
    @Value("${actor.role}")
    private String role;
    @Value("${actor.executerType}")
    private String executerType;

    @Value("${server.port}")
    private int clientPort;

    @Bean
    public AgentConfig configBuilder(Environment env){
        AgentConfig agentConfig = new AgentConfig();

        AgentConfig.Actor actor = new AgentConfig.Actor();
        actor.setExecuterType(executerType);
        actor.setNumberOfActors(numberOfActors);
        actor.setPort(port);
        actor.setRole(role);
        agentConfig.setActor(actor);

        AgentConfig.Job jobInfo = new AgentConfig.Job();
        jobInfo.setArtifact(env.getProperty("job.artifact"));
        jobInfo.setCommand(env.getProperty("job.command"));
        jobInfo.setPath(env.getProperty("job.path"));
        jobInfo.setLogDirectory(env.getProperty("job.logDirectory"));
        jobInfo.setExitValues(new int[]{0,2});
        agentConfig.setJob(jobInfo);

        AgentConfig.LogServer logServer = new AgentConfig.LogServer();
        logServer.setHostName(HostUtils.lookupHost());
        logServer.setPort(clientPort);
        agentConfig.setLogServer(logServer);

        return agentConfig;
    }

    @Bean
    public ActorSystem createActorSystemWithMaster(AgentConfig agentConfig){
        return ClusterFactory.startMaster(2551,"backend",true,agentConfig);
    }


    @Bean
    public ActorRef createRouter(ActorSystem system){
        ActorRef router1 = system.actorOf(new RoundRobinPool(10).props(Props.create(MasterClientActor.class,system)), "router");
        return router1;



    }
}
