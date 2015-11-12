package com.walmart.store.location.endpoint.v1;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.walmart.store.location.data.LocationDataRepository;
import com.walmart.store.location.data.entities.Department;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.walmart.store.location.domain.LocationService;
import com.walmart.store.location.model.Location;

import java.util.Collection;
import java.util.UUID;


/**
 * Created by ahailem on 11/2/15.
 */
@Component
@Path("/location")
public class LocationController {

    private final Logger log = LoggerFactory.getLogger(LocationController.class);

    private LocationService locSvc;

    @Autowired
    private LocationDataRepository locationDataRepository;

    @Autowired
    public LocationController(LocationService locSvc) {
    	this.locSvc = locSvc;
    }
    
    @GET
    @Path("{id}")
    @Produces("application/json")
    @Timed
    @Metered(name="location-metered")
    public Location getLocation(@PathParam("id") long id) {
        log.info("Processing location get request.");

        return locSvc.getLocation(id);    
    }


    @GET
    @Path("/depts/{deptName}")
    @Produces("application/json")
    public Collection<com.walmart.store.location.data.entities.Location> dept(@PathParam("deptName") String name) {
        log.info("Processing location get request using neo.");//"DEPT-1"
        return locationDataRepository.findByDepartmentId(name);
    }

    @GET
    @Path("/locs/{name}")
    @Produces("application/json")
    public Collection<com.walmart.store.location.data.entities.Location> locs(@PathParam("name") String name) {
        log.info("Processing location get request using neo." + name);//"Isle-A"
        return locationDataRepository.findByName(name);
    }
    @GET
    @Path("/locs/factory/{name}")
    @Produces("application/json")
    public Response factory(@PathParam("name") String name){
        Department department = new Department();
        department.setName("Name-"+ name);
        department.setDepartmentId("DEPT-"+name);
        department.setCategory("Category: " + name);

        com.walmart.store.location.data.entities.Location location1 = new com.walmart.store.location.data.entities.Location();
        location1.setGln("gln-"+name);
        location1.setLocationId(UUID.randomUUID());
        location1.setName("Isle-"+name);
        location1.setDepartment(department);
        locationDataRepository.save(location1,1);

        return  Response.ok().entity(location1).build();
    }

}
