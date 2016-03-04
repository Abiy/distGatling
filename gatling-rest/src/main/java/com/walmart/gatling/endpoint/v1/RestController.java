package com.walmart.gatling.endpoint.v1;

import com.google.common.collect.ImmutableMap;

import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.walmart.gatling.commons.Master;
import com.walmart.gatling.commons.ReportExecutor;
import com.walmart.gatling.commons.TrackingResult;
import com.walmart.gatling.data.Entity;
import com.walmart.gatling.domain.DomainService;
import com.walmart.gatling.repository.ServerRepository;
import com.walmart.gatling.repository.ValuePair;

import org.boon.core.Sys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


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
    public Response getServerInfo() {
        Master.ServerInfo info = serverRepository.getServerStatus(new Master.ServerInfo());
        log.info("Processing  get entity request{}", info);
        List<ValuePair> workers = info.getWorkers().entrySet().stream().map(stateEntry ->
                new ValuePair(stateEntry.getValue().status.toString(),
                        stateEntry.getValue().ref.path().name().toString(),
                        stateEntry.getKey())).collect(Collectors.toList());
        return Response.status(Response.Status.ACCEPTED).entity(workers).build();
    }

    @POST
    @Path("/job")
    @Produces("application/json")
    @Timed
    @Metered(name="meter-runJob")
    public Response runJob(JobModel jobModel) {
        String result;
        try {
            result = serverRepository.submitJob(jobModel);
            return Response.status(Response.Status.ACCEPTED).entity( ImmutableMap.of("trackingId",result)).build();
        } catch (Exception e) {
            log.error("Error while submitting user job {}, {}",jobModel,e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Could not submit the job to the cluster master.").build();
        }

    }


    @GET
    @Path("/track/{id}")
    @Produces("application/json")
    @Timed
    @Metered(name="meter-getTrack")
    public Response getTrack(@Context UriInfo uriInfo,@PathParam("id") String trackingId) {
        TrackingResult result;
        try {
            System.out.println("TRACKING ID: " + uriInfo.getPath());
            result = serverRepository.getTrackingInfo(trackingId);
            return Response.status(Response.Status.ACCEPTED).entity( ImmutableMap.of("trackingId",result)).build();
        } catch (Exception e) {
            log.error("Error while submitting user tracking request for: {}, {}",trackingId,e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Could not submit the job to the cluster master.").build();
        }

    }

    @POST
    @Path("/report/{id}")
    @Produces("application/json")
    @Timed
    @Metered(name="meter-postReport")
    public Response postReport(@Context UriInfo uriInfo,@PathParam("id") String trackingId) {
        try {
            ReportExecutor.ReportResult res =  serverRepository.generateReport(trackingId);
            System.out.println(res);
            return Response.status(Response.Status.ACCEPTED).entity(ImmutableMap.of("report", res.result)).build();
        } catch (Exception e) {
            log.error("Error while submitting user report request for: {}, {}",trackingId,e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Could not submit the job to the cluster master.").build();
        }

    }




}
