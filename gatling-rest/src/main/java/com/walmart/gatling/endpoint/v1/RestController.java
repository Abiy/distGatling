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
import java.util.Arrays;
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
    public Entity getServerStatus() {
        Entity entity = domainService.service(1);
        return entity;
    }

    @GET
    @Path("/info")
    @Produces("application/json")
    @Timed
    public Response getServerInfo() {
        Master.ServerInfo info = serverRepository.getServerStatus(new Master.ServerInfo());
        log.info("Processing  get entity request{}", info);

        List<ValuePair> workers = info.getWorkers().entrySet().stream().map(stateEntry ->
                new ValuePair(stateEntry.getValue().status.toString(),
                        stateEntry.getValue().ref.path().name().toString(),
                        stateEntry.getKey())).collect(Collectors.toList());
        return Response.status(Response.Status.ACCEPTED).entity(workers).build();
    }

    @GET
    @Path("/upload/{id}")
    @Produces("application/json")
    @Timed
    public Response getUploadInfo(@PathParam("id") String trackingId) {
        Master.UploadInfo info = serverRepository.getUploadStatus(new Master.UploadInfo(trackingId));
        return Response.status(Response.Status.ACCEPTED).entity(info).build();
    }

    @POST
    @Path("/job")
    @Produces("application/json")
    @Timed
    public Response runJob(JobModel jobModel) {
        String result;
        try {
            result = serverRepository.submitJob(jobModel);
            //String path = "/gatling/server/track/" + result;
            String path = "#/tracker/" + result;
            return Response.status(Response.Status.ACCEPTED).entity( ImmutableMap.of("trackingPath",path)).build();
        } catch (Exception e) {
            log.error("Error while submitting user job {}, {}",jobModel,e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Could not submit the job to the cluster master.").build();
        }

    }


    @GET
      @Path("/track/{id}")
      @Produces("application/json")
      @Timed
      public Response getTrack(@Context UriInfo uriInfo,@PathParam("id") String trackingId) {
        TrackingResult result;
        try {
            result = serverRepository.getTrackingInfo(trackingId);
            return Response.status(Response.Status.ACCEPTED).entity( ImmutableMap.of("trackingInfo",result)).build();
        } catch (Exception e) {
            log.error("Error fetching tracking info for: {}, {}",trackingId,e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Error fetching tracking info.").build();
        }

    }

    @POST
    @Path("/report/{id}")
    @Produces("application/json")
    @Timed
    public Response postReport(@Context UriInfo uriInfo,@PathParam("id") String trackingId) {
        try {
            ReportExecutor.ReportResult res =  serverRepository.generateReport(trackingId);
            log.info("report result: {}",res);
            return Response.status(Response.Status.ACCEPTED).entity(ImmutableMap.of("report", res.result)).build();
        } catch (Exception e) {
            log.error("Error while submitting user report request for: {}, {}",trackingId,e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Error while submitting user report request.").build();
        }

    }

    @POST
    @Path("/abort/{id}")
    @Produces("application/json")
    @Timed
    public Response postCancel(@Context UriInfo uriInfo,@PathParam("id") String trackingId) {
        try {
            boolean res = serverRepository.abortJob(trackingId);
            log.info("Cancel result: {}",res);
            return Response.status(Response.Status.CREATED).entity(ImmutableMap.of("cancelled", res)).build();
        } catch (Exception e) {
            log.error("Error while submitting cancel job request for: {}, {}",trackingId,e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Error while submitting cancel job request.").build();
        }

    }
    @GET
    @Path("/abort")
    @Produces("application/json")
    @Timed
    public Response getAbortStatus(@Context UriInfo uriInfo,@QueryParam("trackingId") String trackingId) {
        boolean result;
        try {
            result = serverRepository.getTrackingInfo(trackingId).isCancelled();
            return Response.status(Response.Status.ACCEPTED).entity( result).build();
        } catch (Exception e) {
            log.error("Error while submitting abort status request for: {}, {}",trackingId,e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Error while submitting abort status request.").build();
        }

    }




}
