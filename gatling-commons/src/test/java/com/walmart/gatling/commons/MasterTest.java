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

package com.walmart.gatling.commons;

import akka.testkit.TestKit;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.persistence.SnapshotOffer;
import akka.persistence.journal.leveldb.LeveldbJournal;
import akka.persistence.journal.leveldb.LeveldbStore;
import akka.persistence.journal.leveldb.SharedLeveldbJournal;
import akka.persistence.journal.leveldb.SharedLeveldbStore;
import akka.testkit.JavaTestKit;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by walmart
 */
public class MasterTest  {

    protected static AgentConfig agentConfig;
    protected static ActorSystem system;
    protected static ActorRef master;

    short parallelism = 1;
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
                .withHasDataFeed(false)
                .withParameterString("")
                .withFileFullName("FileFullName")
                .withDataFileName("DataFileName")
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
        final Props props = Master.props(new FiniteDuration(20, TimeUnit.SECONDS), agentConfig);
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


    protected Master.Job getJob() {
        String id = UUID.randomUUID().toString();
        taskEvent.setJobName("gatling");
        Master.Job job = new Master.Job("projectName", taskEvent, id, "","simulatioFilePath","dataFilePath", "bodiesFilePath", false);
        return job;
    }

}


