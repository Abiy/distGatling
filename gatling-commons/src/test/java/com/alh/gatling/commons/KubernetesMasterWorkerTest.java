package com.alh.gatling.commons;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;

import akka.testkit.javadsl.TestKit;

import java.util.UUID;

public class KubernetesMasterWorkerTest extends KubernetesMasterTest {

    public static final String PROJECT_NAME = "projectName";


    @Test
    public void testSubmitJobAndRegisterWorkerKubernetes() {
        new TestKit(system) {
            {
                String trackingId = UUID.randomUUID().toString();
                KubernetesService kubernetesService = mock(KubernetesService.class);
                when(kubernetesService.createDeploy(anyString())).thenAnswer(invocation -> { return "gatling-worker." + invocation.getArgument(0); });
                master.tell(new Master.SubmitNewSimulation(trackingId, kubernetesService), getRef());
                final Master.Ack ack = expectMsgClass(Master.Ack.class);//assert ack
                Assert.assertEquals(trackingId, ack.getWorkId());

                Master.Job job1 = getJob(trackingId);
                master.tell(job1, getRef());//send job
                final Master.Ack ack1 = expectMsgClass(Master.Ack.class);//assert ack
                Assert.assertEquals(job1.jobId, ack1.getWorkId());

                Master.Job job2 = getJob(trackingId);
                master.tell(job2, getRef());//send job
                final Master.Ack ack2 = expectMsgClass(Master.Ack.class);//assert ack
                Assert.assertEquals(job2.jobId, ack2.getWorkId());

                //send register worker
                master.tell(new MasterWorkerProtocol.RegisterWorker("gatling-worker." + job1.jobId), getRef());
                master.tell(new MasterWorkerProtocol.RegisterWorker("gatling-worker." + job2.jobId), getRef());

                MasterWorkerProtocol.WorkIsReady ready1 = expectMsgEquals(
                    MasterWorkerProtocol.WorkIsReady.getInstance());
                Assert.assertTrue(ready1.equals(MasterWorkerProtocol.WorkIsReady.getInstance()));
                MasterWorkerProtocol.WorkIsReady ready2 = expectMsgEquals(
                    MasterWorkerProtocol.WorkIsReady.getInstance());
                Assert.assertTrue(ready2.equals(MasterWorkerProtocol.WorkIsReady.getInstance()));
            }
        };
    }

    @Test
    public void testSubmitJobAndWorkerRequestWorkKubernetes() {
        new TestKit(system) {
            {
                //simulation ID
                String trackingId = UUID.randomUUID().toString();

                //send kubernetes service for this simulation
                KubernetesService kubernetesService = mock(KubernetesService.class);
                when(kubernetesService.createDeploy(anyString())).thenAnswer(invocation -> { return "gatling-worker." + invocation.getArgument(0); });
                master.tell(new Master.SubmitNewSimulation(trackingId, kubernetesService), getRef());
                final Master.Ack ack = expectMsgClass(Master.Ack.class);//assert ack
                Assert.assertEquals(trackingId, ack.getWorkId());

                //create and send to master 2 jobs( because of parallelism=2)
                Master.Job job1 = getJob(trackingId);
                master.tell(job1, getRef());//send job
                final Master.Ack ack1 = expectMsgClass(Master.Ack.class);//assert ack
                Assert.assertEquals(job1.jobId, ack1.getWorkId());

                Master.Job job2 = getJob(trackingId);
                master.tell(job2, getRef());//send job
                final Master.Ack ack2 = expectMsgClass(Master.Ack.class);//assert ack
                Assert.assertEquals(job2.jobId, ack2.getWorkId());

                // worker notify master to register
                master.tell(new MasterWorkerProtocol.RegisterWorker("gatling-worker." + job1.jobId), getRef());
                master.tell(new MasterWorkerProtocol.RegisterWorker("gatling-worker." + job2.jobId), getRef());

                MasterWorkerProtocol.WorkIsReady ready1 = expectMsgEquals(
                    MasterWorkerProtocol.WorkIsReady.getInstance());
                Assert.assertTrue(ready1.equals(MasterWorkerProtocol.WorkIsReady.getInstance()));
                MasterWorkerProtocol.WorkIsReady ready2 = expectMsgEquals(
                    MasterWorkerProtocol.WorkIsReady.getInstance());
                Assert.assertTrue(ready2.equals(MasterWorkerProtocol.WorkIsReady.getInstance()));

                // worker request work
                master.tell(new MasterWorkerProtocol.WorkerRequestsWork("gatling-worker." + job1.jobId, PROJECT_NAME),
                            getRef());//send request
                final Master.Job job3 = expectMsgClass(Master.Job.class);//assert Job
                Assert.assertEquals(job1.jobId, job3.jobId);

                master.tell(new MasterWorkerProtocol.WorkerRequestsWork("gatling-worker." + job2.jobId, PROJECT_NAME),
                            getRef());//send request
                final Master.Job job4 = expectMsgClass(Master.Job.class);//assert Job
                Assert.assertEquals(job2.jobId, job4.jobId);

                //check current tracking results
                master.tell(new Master.TrackingInfo(job1.trackingId), getRef());//send request
                TrackingResult trackingResult = expectMsgClass(TrackingResult.class);
                Assert.assertEquals(2, trackingResult.getInProgressCount());
                Assert.assertEquals(0, trackingResult.getPendingCount());

                // worker send to master work is done message
                master.tell(new MasterWorkerProtocol.WorkIsDone("gatling-worker." + job1.jobId, job3.jobId,
                                                                new Worker.Result(1, "", "", "", job1)),
                            getRef());//send update
                expectMsgClass(KubernetesMaster.AckKubernetes.class);//assert ack

                //check current tracking results
                master.tell(new Master.TrackingInfo(job1.trackingId), getRef());//send request
                trackingResult = expectMsgClass(TrackingResult.class);
                Assert.assertEquals(1, trackingResult.getInProgressCount());
                Assert.assertEquals(0, trackingResult.getPendingCount());
                Assert.assertEquals(1, trackingResult.getCompleted().size());
            }
        };
    }

    @Test
    public void testWorkerRequestsFileBeforeAndAfterActiveFileExists() {
        new TestKit(system) {
            {
                master.tell(new MasterWorkerProtocol.WorkerRequestsFile("worker-testWorkerRequestsFile", "projectName",
                                                                        "127.0.0.1"), getRef());//send request
                expectNoMsg();

                master.tell(new Master.UploadFile("trId1", "/path", "file", "role", "lib"),
                            getRef());//send request
                Master.Ack ack = expectMsgClass(Master.Ack.class);//assert ack
                Assert.assertEquals("trId1", ack.getWorkId());

                master.tell(new Master.UploadFile("trId2", "/path", "file", "role", "lib"),
                            getRef());//send request
                ack = expectMsgClass(Master.Ack.class);//assert ack
                Assert.assertEquals("trId2", ack.getWorkId());

                master.tell(new MasterWorkerProtocol.WorkerRequestsFile("worker-testWorkerRequestsFile", "projectName",
                                                                        "127.0.0.1"), getRef());//send request
                final Master.FileJob fileJob = expectMsgClass(Master.FileJob.class);//assert ack
            }
        };
    }

    @Test
    public void testWorkerSendsFileUploadComplete() {
        new TestKit(system) {
            {
                master.tell(new Worker.FileUploadComplete(new Master.UploadFile("trId", "/path", "file", "role", "lib"),
                                                          "127.0.0.1"), getRef());//send request
                expectNoMsg();
            }
        };
    }


}
