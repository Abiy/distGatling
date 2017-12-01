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

package com.walmart.gatling.endpoint.v1;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import com.walmart.gatling.commons.AgentConfig;
import com.walmart.gatling.commons.JobSummary;
import com.walmart.gatling.commons.Master;
import com.walmart.gatling.commons.ReportExecutor;
import com.walmart.gatling.commons.TrackingResult;
import com.walmart.gatling.domain.DashboardModel;
import com.walmart.gatling.domain.SimulationJobModel;
import com.walmart.gatling.domain.WorkerModel;
import com.walmart.gatling.service.PageUtils;
import com.walmart.gatling.service.ServerRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by walmart
 */
@Component
@Path("/server")
public class RestController {
    private final Logger log = LoggerFactory.getLogger(RestController.class);
    private ServerRepository serverRepository;

    @Autowired
    private AgentConfig agentConfig;

    @Autowired
    public RestController(ServerRepository serverRepository) {
    	this.serverRepository = serverRepository;
    }


    /**
     * This an end point to fetch cluster status, provides information on the status of each worker
     * @return
     */
    @GET
    @Path("/info")
    @Produces("application/json")
    @Timed
    public Response getServerInfo() {
        List<WorkerModel> workers = getWorkersInfo();
        return Response.status(Response.Status.OK).entity(workers).build();
    }

    @GET
    @Path("/workers/host")
    @Produces("application/json")
    @Timed
    public Response getWorkerHostInfo() {
        List<WorkerModel> workers = getWorkersInfo();
        Map<String, Long> result = workers.stream()
                .collect(Collectors.groupingBy(WorkerModel::getHost, Collectors.counting()));
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @GET
    @Path("/workers/partition")
    @Produces("application/json")
    @Timed
    public Response getWorkerPartitionInfo() {
        List<WorkerModel> workers = getWorkersInfo();
        Map<String, Long> result = workers.stream()
                .collect(Collectors.groupingBy(WorkerModel::getRole, Collectors.counting()));
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @GET
    @Path("/workers/status")
    @Produces("application/json")
    @Timed
    public Response getWorkerStatusInfo() {
        List<WorkerModel> workers = getWorkersInfo();
        Map<String, Long> result = workers.stream()
                .collect(Collectors.groupingBy(WorkerModel::getStatus, Collectors.counting()));
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @GET
    @Path("/dashboard")
    @Timed
    public Response getDashboardInfo() {
        DashboardModel dashboard = new DashboardModel();
        List<WorkerModel> workers = getWorkersInfo();

        Map<String, Long> status = workers.stream()
                .collect(Collectors.groupingBy(WorkerModel::getStatus, Collectors.counting()));
        dashboard.setStatus(status);

        Map<String, Long> partition = workers.stream()
                .collect(Collectors.groupingBy(WorkerModel::getRole, Collectors.counting()));
        dashboard.setPartition(partition);

        Map<String, Long> host = workers.stream()
                .collect(Collectors.groupingBy(WorkerModel::getHost, Collectors.counting()));
        dashboard.setHost(host);

        Map<String, Long> partitionStatus = workers.stream()
                .collect(Collectors.groupingBy(p->p.getRole() + ":" + p.getStatus(), Collectors.counting()));
        dashboard.setPartitionStatus(partitionStatus);

        return Response.status(Response.Status.OK).entity(dashboard).build();
    }

    @GET
    @Path("/workers/partition/status")
    @Produces("application/json")
    @Timed
    public Response getWorkerPartitionStatusInfo() {
        List<WorkerModel> workers = getWorkersInfo();
        Map<String, Long> result = workers.stream()
                .collect(Collectors.groupingBy(p->p.getRole() + ":" + p.getStatus(), Collectors.counting()));
        return Response.status(Response.Status.OK).entity(result).build();
    }

    private List<WorkerModel> getWorkersInfo() {
        Master.ServerInfo info = serverRepository.getServerStatus(new Master.ServerInfo());
        log.info("Processing  get cluster status request: {}", info);
        return info.getWorkers().entrySet().stream().map(stateEntry ->
                new WorkerModel(stateEntry.getValue().status.toString(),
                        stateEntry.getValue().ref.path().name().toString(),
                        stateEntry.getKey())).collect(Collectors.toList());
    }

    @GET
    @Path("/running/summary")
    @Produces("application/json")
    @Timed
    public Response getRunningSummary(@QueryParam("size") int size,@QueryParam("page") int page) {
        PageRequest  pageRequest = PageUtils.getPageRequest(size, page, null);
        List<JobSummary> summaries = serverRepository.getJobSummary().stream().filter(s -> s.runningJob()).collect(Collectors.toList());
        List<JobSummary> pagedResult = summaries.stream()
                .filter(p->p.runningJob())
                .sorted((o1, o2) -> Long.valueOf(o2.getStartTime()).compareTo(Long.valueOf(o1.getStartTime())))
                .skip(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .collect(Collectors.toList());

        Page<JobSummary> result = new PageImpl<>(pagedResult, pageRequest, summaries.size());
        log.info("Processing  get cluster job summary request.");
        return Response.status(Response.Status.OK).entity(result).build();
    }


    @GET
    @Path("/completed/summary")
    @Produces("application/json")
    @Timed
    public Response getCompletedJobSummary(@QueryParam("size") int size,@QueryParam("page") int page ) {
        PageRequest  pageRequest = PageUtils.getPageRequest(size, page,null);
        List<JobSummary> summaries = serverRepository.getJobSummary();
        List<JobSummary> pagedResult = summaries.stream()
                .filter(p->!p.runningJob())
                .sorted((o1, o2) -> Long.valueOf(o2.getStartTime()).compareTo(Long.valueOf(o1.getStartTime())))
                .skip(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .collect(Collectors.toList());
        Page<JobSummary> result = new PageImpl<>(pagedResult, pageRequest, summaries.size());
        log.info("Processing  get cluster job summary request.");
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @GET
    @Path("/detail/{trackingId}")
    @Produces("application/json")
    @Timed
    public Response getJobDetail(@PathParam("trackingId") String trackingId ) {
        List<JobSummary> summaries = serverRepository.getJobSummary();
        Optional<JobSummary> summary = summaries.stream().filter(p -> p.getJobInfo().trackingId.equalsIgnoreCase(trackingId)).findFirst();
        log.info("Processing  get job detail.");
        if(summary.isPresent())
            return Response.status(Response.Status.OK).entity(summary.get()).build();
        else
            return Response.status(Response.Status.BAD_REQUEST).entity("The Specified tracking id is not available.").build();
    }

    @GET
    @Path("/upload/{id}")
    @Produces("application/json")
    @Timed
    public Response getUploadInfo(@PathParam("id") String trackingId) {
        Optional<Master.UploadInfo> info = serverRepository.getUploadStatus(new Master.UploadInfo(trackingId));
        return Response.status(Response.Status.ACCEPTED).entity(info).build();
    }

    /**
     * Requests the master to run gatling job on the workers, if the job is submitted properly
     * return the tracker page with the tracking id generated by the master.
     * The master actor generates a unique tracking id for each gatling job.
     * @param simulationJobModel
     * @return
     */
    @POST
    @Path("/job")
    @Produces("application/json")
    @Timed
    public Response runSimulationJob(SimulationJobModel simulationJobModel) {
        Optional<String> result;
        try {
            result = serverRepository.submitSimulationJob(simulationJobModel);
            String path = "#/tracker/" + result.get();
            return Response.status(Response.Status.ACCEPTED).entity( ImmutableMap.of("trackingPath",path)).build();
        } catch (Exception e) {
            log.error("Error while submitting user job {}, {}", simulationJobModel,e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Could not submit the job to the cluster master.").build();
        }

    }


    /**
     * Given a tracking id returns the current status of the gatling simulation
     * @param uriInfo
     * @param trackingId
     * @return
     */
    @GET
    @Path("/track/{id}")
    @Produces("application/json")
    @Timed
    public Response getTrack(@Context UriInfo uriInfo, @PathParam("id") String trackingId) {
        TrackingResult result;
        try {
            result = serverRepository.getTrackingInfo(trackingId);
            return Response.status(Response.Status.ACCEPTED).entity(ImmutableMap.of("trackingInfo", result)).build();
        } catch (Exception e) {
            log.error("Error fetching tracking info for: {}, {}", trackingId, e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Error fetching tracking info.").build();
        }

    }

    /**
     * Instructs  the master to collect all the logs across all the workers and
     * generate gatling report for the given tracking id
     * @param uriInfo
     * @param trackingId
     * @return
     */
    @POST
    @Path("/report/{id}")
    @Produces("application/json")
    @Timed
    public Response postReport(@Context UriInfo uriInfo,@PathParam("id") String trackingId) {
        try {
            Optional<ReportExecutor.ReportResult> res =  serverRepository.generateReport(trackingId);
            log.info("report result: {}",res);
            return Response.status(Response.Status.ACCEPTED).entity(ImmutableMap.of("report", agentConfig.getGenericUrl(res.get().result.toString(), StringUtils.EMPTY,StringUtils.EMPTY) )).build();
        } catch (Exception e) {
            log.error("Error while submitting user report request for: {}, {}",trackingId,e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Error while submitting user report request.").build();
        }

    }

    /**
     * Instructs the master to cancel a running gatling simulation across all workers
     * @param uriInfo
     * @param trackingId
     * @return
     */
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

    /**
     * The workers poll the master every minute to determine if the given simulation
     * which is identified by the tracking id is cancelled by the user
     *
     * If the user has cancelled the simulation job workers will terminate the running simulation
     * @param uriInfo
     * @param trackingId
     * @return
     */
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
