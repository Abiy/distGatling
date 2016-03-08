package com.walmart.gatling.repository;

import com.walmart.gatling.commons.Master;
import com.walmart.gatling.commons.MasterClientActor;
import com.walmart.gatling.commons.ReportExecutor;
import com.walmart.gatling.commons.TaskEvent;
import com.walmart.gatling.commons.TrackingResult;
import com.walmart.gatling.domain.DomainService;
import com.walmart.gatling.endpoint.v1.JobModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.util.Timeout;
import javafx.util.Pair;
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

    private Object sendToMaster(Object message,int timeoutInSeconds) {
        Timeout timeout = new Timeout(timeoutInSeconds, TimeUnit.SECONDS);
        Future<Object> future = ask(router, message, timeout);
        try {
            Object info =  Await.result(future, timeout.duration());
            if(info instanceof MasterClientActor.Ok) {
                MasterClientActor.Ok ok = (MasterClientActor.Ok)info;
                log.debug("Ok Message from server just got here: {}", info);
                return ok.getMsg();
            }
        } catch (Exception e) {
            log.error("Error fetching status from server {}", e);
        }
        return null;
    }

    public Master.ServerInfo getServerStatus(Object message) {
        Object result = sendToMaster(message,6);
        if(result!=null && result instanceof Master.ServerInfo) {
            Master.ServerInfo info = (Master.ServerInfo)result;
            return info;
        }
        return new Master.ServerInfo();
    }

    public String submitJob(JobModel jobModel) throws Exception {
        String trackingId = UUID.randomUUID().toString();

        TaskEvent taskEvent = new TaskEvent();
        taskEvent.setJobName("gatling");
        taskEvent.setRoleName(jobModel.getRoleId());
        taskEvent.setParameters(Arrays.asList(new Pair<>("0", "-nr"),
                new Pair<>("1", "-s"), new Pair<>("2", jobModel.getSimulation())
        ));
        //cmdLine.addArgument("-rf").addArgument(agentConfig.getJob().getResultPath(job.roleId,job.jobId));
        Timeout timeout = new Timeout(6, TimeUnit.SECONDS);
        int success=0;
        for(int i=0;i<jobModel.getCount();i++) {
            Future<Object> future = ask(router, new Master.Job (jobModel.getRoleId(), taskEvent, trackingId), timeout);
            Object result =  Await.result(future, timeout.duration());
            if(result instanceof MasterClientActor.Ok) {
                success++;
                log.debug("Ok Message from server just got here: {}", result);
            }
        }

        if(success==jobModel.getCount()) {
            log.debug("Job Successfully submitted to master");
            return trackingId;
        }
        return null;
    }

    public TrackingResult getTrackingInfo(String trackingId) {
        Object result = sendToMaster(new Master.TrackingInfo(trackingId),6);
        if(result!=null && result instanceof TrackingResult) {
            TrackingResult info = (TrackingResult)result;
            return info;
        }
        return new TrackingResult(0,0);
    }

    public ReportExecutor.ReportResult generateReport(String trackingId) {

        TaskEvent taskEvent = new TaskEvent();
        taskEvent.setJobName("gatling");
        taskEvent.setRoleName("report");
        taskEvent.setParameters(Arrays.asList(
                new Pair<>("0", "-ro")
        ));
        Object result = sendToMaster(new Master.Report(trackingId,taskEvent),10);

        log.info("Report generated {}",result);
        if(result!=null && result instanceof ReportExecutor.ReportResult) {
            log.info("Report generated accurately {}",result);
            return (ReportExecutor.ReportResult)result;
        }
        return null;
    }

    public String uploadFile(String path, String name, String role, String type) {
        String trackingId = UUID.randomUUID().toString();
        Master.UploadFile uploadFileRequest = new Master.UploadFile(trackingId,path,name,role,type);
        Object result = sendToMaster(uploadFileRequest,5);
        log.info("UploadFile request sent {}",result);
        if(result!=null && result instanceof Master.Ack) {
            return ((Master.Ack)result).getWorkId();
        }
        return null;
    }

    public Master.UploadInfo getUploadStatus(Master.UploadInfo uploadInfo) {
        Object result = sendToMaster(uploadInfo,5);
        if(result!=null && result instanceof Master.UploadInfo) {
            return ((Master.UploadInfo)result);
        }
        return null;
    }
}
