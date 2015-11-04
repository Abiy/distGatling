package com.walmart.store.location.endpoint.v1;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codahale.metrics.annotation.Metric;
import com.walmart.store.location.domain.LocationService;
import com.walmart.store.location.model.Location;

/**
 * Created by ahailem on 11/2/15.
 */
@Component
@Path("/location")
public class LocationController {

    private final Logger log = LoggerFactory.getLogger(LocationController.class);

    private LocationService locSvc;
    
    @Autowired
    public LocationController(LocationService locSvc) {
    	this.locSvc = locSvc;
    }
    
    @GET
    @Path("{id}")
    @Produces("application/json")
    @Metric
    public Location getLocation(@PathParam("id") long id) {
        log.info("Processing location get request.");

        return locSvc.getLocation(id);    
    }

}
