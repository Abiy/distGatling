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

package com.walmart.gatling;

import com.walmart.gatling.commons.AgentConfig;
import com.walmart.gatling.commons.HostUtils;
import com.walmart.gatling.commons.MasterClientActor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.RoundRobinPool;

/**
 * Created by walmart
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

    @Value("${akka.contact-points}")
    private String contactPoints;

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
       jobInfo.setJobDirectory(env.getProperty("job.jobDirectory"));
       jobInfo.setExitValues(new int[]{0,2,1});
       agentConfig.setJob(jobInfo);

       AgentConfig.LogServer logServer = new AgentConfig.LogServer();
       logServer.setHostName(HostUtils.lookupIp());
       logServer.setPort(clientPort);
       agentConfig.setLogServer(logServer);
       agentConfig.setContactPoint( contactPoints);

       return agentConfig;
   }

    /**
     * Bean factory that creates the actor system with workers and joins the cluster/master
     * using the contact-point property value
     * @param agentConfig
     * @return
     */
    @Bean
    public ActorSystem createActorSystemWithAgent(AgentConfig agentConfig){
        return WorkerFactory.startWorkersWithExecutors(agentConfig);
    }

}
