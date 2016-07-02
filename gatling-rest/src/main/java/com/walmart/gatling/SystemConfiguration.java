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
 * Created by walmart. A spring configuration object to create beans
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


    /**
     * bean factory to create the agent configuration
     * @param env
     * @return
     */
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
        logServer.setHostName(HostUtils.lookupIp());
        logServer.setPort(clientPort);
        agentConfig.setLogServer(logServer);

        return agentConfig;
    }

    /**
     * bean factory to create the actor system
     * @param agentConfig
     * @param port
     * @param name
     * @param isPrimary
     * @return
     */
    @Bean
    public ActorSystem createActorSystemWithMaster(AgentConfig agentConfig,
                                                   @Value("${master.port}") int port,
                                                   @Value("${master.name}") String name,
                                                   @Value("${master.primary}") boolean isPrimary) {

        return ClusterFactory.startMaster(port,name,isPrimary,agentConfig);
    }


    /**
     * bean factory to create pool of the master client actors, the pool is used in a round robin manner
     * @param system
     * @param pool
     * @return
     */
    @Bean
    public ActorRef createRouter(ActorSystem system,@Value("${master.client.pool}") int pool){
        ActorRef router1 = system.actorOf(new RoundRobinPool(pool).props(Props.create(MasterClientActor.class,system)), "router");
        return router1;
    }
}
