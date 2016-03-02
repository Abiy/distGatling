package com.walmart.gatling.endpoint.v1;

import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.walmart.gatling.commons.Master;
import com.walmart.gatling.data.Entity;
import com.walmart.gatling.domain.DomainService;
import com.walmart.gatling.repository.ServerRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;


/**
 * Created by walmart
 */
@Component
@Path("/server")
public class RestController {

    private final Logger log = LoggerFactory.getLogger(RestController.class);

    private ServerRepository serverRepository;
    private DomainService domainService;


    @Autowired
    public RestController(ServerRepository serverRepository, DomainService domainService) {
    	this.serverRepository = serverRepository;
        this.domainService = domainService;
    }
    

    @GET
    @Path("/status")
    @Produces("application/json")
    @Timed
    @Metered(name="meter-getServerStatus")
    public Entity getServerStatus() {
        Entity entity = domainService.service(1);
        return entity;
    }

    @GET
    @Path("/info")
    @Produces("application/json")
    @Timed
    @Metered(name="meter-getServerInfo")
    public Master.ServerInfo getServerInfo() {
        Master.ServerInfo info = serverRepository.getServerStatus(new Master.ServerInfo("234324", "roleId"));
        log.info("Processing  get entity request{}", info);
        return info;
    }




}
