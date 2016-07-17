package com.walmart.gatling.commons;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.testkit.TestActorRef;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import static akka.pattern.Patterns$.*;
import static org.junit.Assert.*;

/**
 * Created by ahailem on 7/6/16.
 */
public class MasterTest {

    private ActorSystem system;
    private ActorRef masterRef;
    private AgentConfig agentConfig;

    @Before
    public  void setupActorSystem() throws Exception {

        AgentConfig agentConfig = new AgentConfig();
        system = startMaster(agentConfig);
        //masterRef = system.actorOf(Master.props(FiniteDuration.Zero(), agentConfig), "master");

    }

    @Test
    public void testRegisterWorker() throws Exception {
        final Props props = Master.props(FiniteDuration.Zero(), agentConfig);
        final TestActorRef<Master> ref = TestActorRef.create(system, props, "master");
        final Future<Object> future = akka.pattern.Patterns.ask(ref, new MasterWorkerProtocol.RegisterWorker("worker1"), 3000);
        assertTrue(future.isCompleted());
        assertEquals(MasterWorkerProtocol.WorkIsReady.getInstance().hashCode(), Await.result(future, Duration.Zero()).hashCode());
    }

    public static ActorSystem startMaster(AgentConfig agentConfig) {
        String ip = HostUtils.lookupIp();
        String seed = String.format("akka.cluster.seed-nodes=[\"akka.tcp://%s@%s:%s\"]", Constants.PerformanceSystem, ip, 2552);
        Config conf = ConfigFactory.parseString("akka.cluster.roles=[backend]").
                withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=2551")).
                withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname=" + ip)).
                withFallback(ConfigFactory.parseString(seed)).
                withFallback(ConfigFactory.load("application"));

        ActorSystem system = ActorSystem.create(Constants.PerformanceSystem, conf);
        return system;
    }

    @After
    public void closeActorSystem() {
         system.shutdown();
    }
}
