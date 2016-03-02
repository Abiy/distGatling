package com.walmart.gatling.repository;

import com.walmart.gatling.commons.Master;
import com.walmart.gatling.commons.MasterClientActor;
import com.walmart.gatling.domain.DomainService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;

import static akka.pattern.Patterns.ask;

/**
 * Created by walmart
 */
@Component
public class ServerRepository {

    private final Logger log = LoggerFactory.getLogger(ServerRepository.class);

    private DomainService domainService;
    private ActorRef router;

    @Autowired
    public ServerRepository(DomainService domainService, ActorRef router) {
        this.domainService = domainService;
        this.router = router;
    }

    public Master.ServerInfo getServerStatus(Object message) {

        for(int i=1;i<10;i++)
            log.debug("Message fo be sent to master {}", message);

        Timeout timeout = new Timeout(6, TimeUnit.SECONDS);
        Future<Object> future = ask(router, message, timeout);
        try {
            Object info =  Await.result(future, timeout.duration());
            if(info instanceof MasterClientActor.Ok) {
                MasterClientActor.Ok ok = (MasterClientActor.Ok)info;
                log.debug("Ok Message from server just got here: {}", info);
                return (Master.ServerInfo)ok.getMsg();
            }
        } catch (Exception e) {
            log.error("Error fetching status from server {}", e);
        }
        return new Master.ServerInfo("","");


    }
}
