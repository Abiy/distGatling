package com.walmart.gatling.endpoint.v1;

import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.walmart.gatling.data.DataRepository;
import com.walmart.gatling.data.Entity;
import com.walmart.gatling.domain.DomainService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;


/**
 * Created by walmart
 */
@Component
@Path("/v1/template")
public class RestController {

    private final Logger log = LoggerFactory.getLogger(RestController.class);

    private DomainService domainService;

    @Autowired
    private DataRepository dataRepository;

    @Autowired
    public RestController(DomainService locSvc) {
    	this.domainService = locSvc;
    }
    
    @GET
    @Path("/{id}")
    @Produces("application/json")
    @Timed
    @Metered(name="meter-getEntity")
    public Entity getEntity(@PathParam("id") long id) {
        log.info("Processing  get entity request.");
        Entity entity = domainService.service(id);
        return entity;
    }



}
