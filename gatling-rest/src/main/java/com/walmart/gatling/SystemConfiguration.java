package com.walmart.gatling;

import com.walmart.gatling.commons.MasterClientActor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.FromConfig;
import akka.routing.RoundRobinPool;

/**
 * Created by walmart.
 */
@Configuration
public class SystemConfiguration {

    @Bean
    public ActorSystem createActorSystemWithMaster(){
        return ClusterFactory.startMaster(2551,"backend",true);
    }


    @Bean
    public ActorRef createRouter(ActorSystem system){
        //ActorRef router1 = system.actorOf(FromConfig.getInstance().props(Props.create(MasterClientActor.class)), "createRouter");
        ActorRef router1 = system.actorOf(new RoundRobinPool(1).props(Props.create(MasterClientActor.class,system)), "router");
        return router1;



    }
}
