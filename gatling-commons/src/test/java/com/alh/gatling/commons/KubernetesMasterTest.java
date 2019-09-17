package com.alh.gatling.commons;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.persistence.journal.leveldb.SharedLeveldbStore;
import akka.testkit.TestKit;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class KubernetesMasterTest  {

    protected static AgentConfig agentConfig;
    protected static ActorSystem system;
    protected static ActorRef master;

    public short parallelism = 2;
    JobSummary.JobInfo jobinfo;
    TaskEvent taskEvent;
    @Before
    public void setUp(){
        jobinfo = JobSummary.JobInfo.newBuilder()
            .withCount(parallelism)
            .withJobName("gatling")
            .withPartitionAccessKey("noAccessKey")
            .withPartitionName("public")
            .withUser("testUser")
            .withTrackingId(UUID.randomUUID().toString())
            .withParameterString("")
            .withFileFullName("FileFullName")
            .build();
        taskEvent = new TaskEvent();
        {
            taskEvent.setJobName("gatling"); //the gatling.sh script is the gateway for simulation files
            taskEvent.setJobInfo(jobinfo);
            taskEvent.setParameters(new ArrayList<>());
        }
    }
    @BeforeClass
    public static void setupActorSystem() throws Exception {

        FileUtils.deleteDirectory(new File("journal"));
        FileUtils.deleteDirectory(new File("shared-journal"));
        FileUtils.deleteDirectory(new File("snapshots"));
        //file.isDirectory()
        agentConfig = new AgentConfig();
        AgentConfig.LogServer log = new AgentConfig.LogServer();
        log.setHostName("127.0.0.1");
        log.setPort(8080);
        agentConfig.setLogServer(log);
        system = startMaster();



        final Props props = KubernetesMaster.props(new FiniteDuration(20, TimeUnit.SECONDS), agentConfig);
        master = system.actorOf(props, "master");
    }

    @AfterClass
    public static void teardownClass() {
        TestKit.shutdownActorSystem(system, Duration.create(10, TimeUnit.SECONDS), true);
        system = null;
    }

    public static ActorSystem startMaster() {
        String ip = "127.0.0.1";
        String seed = String.format("akka.cluster.seed-nodes=[\"akka.tcp://%s@%s:%s\"]", Constants.PerformanceSystem, ip, 2551);
        Config conf = ConfigFactory.parseString("akka.cluster.roles=[backend]").
            withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=2551")).
            withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname=" + ip)).
            withFallback(ConfigFactory.parseString(seed)).
            withFallback(ConfigFactory.load("application"));

        system = ActorSystem.create(Constants.PerformanceSystem, conf);
        system.actorOf(Props.create(SharedLeveldbStore.class), "store");
        return system;
    }


    protected Master.Job getJob(String trackingId) {
        taskEvent.setJobName("gatling");

        KubernetesService kubernetesService = mock(KubernetesService.class);
        when(kubernetesService.createDeploy(anyString())).thenAnswer(invocation -> { return "gatling-worker." + invocation.getArgument(0); });
        Master.Job job = new Master.Job("projectName", taskEvent, trackingId, "","simulationFilePath", "resourcesFilePath", false,parallelism);
        return job;
    }

}


