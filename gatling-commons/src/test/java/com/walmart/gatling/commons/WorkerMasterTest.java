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

import akka.testkit.javadsl.TestKit;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

/**
 *
 */
public class WorkerMasterTest extends MasterTest {

    public static final String PROJECT_NAME = "projectName";


    @Test
    public void testSubmitJobAndRegisterWorker() {
        new TestKit(system) {
            {
                Master.Job job = getJob();
                master.tell(job, getRef());//send job
                final Master.Ack ack = expectMsgClass(Master.Ack.class);//assert ack
                Assert.assertEquals(job.jobId, ack.getWorkId());
                //send register worker
                master.tell(new MasterWorkerProtocol.RegisterWorker(UUID.randomUUID().toString()), getRef());
                MasterWorkerProtocol.WorkIsReady ready = expectMsgEquals(MasterWorkerProtocol.WorkIsReady.getInstance());

                Assert.assertTrue(ready.equals(MasterWorkerProtocol.WorkIsReady.getInstance()));

            }
        };
    }


    @Test
    public void testWorkerRequestsFileBeforeAndAfterActiveFileExists() {
        new TestKit(system) {
            {
                master.tell(new MasterWorkerProtocol.WorkerRequestsFile("worker-testWorkerRequestsFile","projectName","127.0.0.1"), getRef());//send request
                expectNoMsg();

                master.tell(new Master.UploadFile("trId1","/path","file","role","lib"), getRef());//send request
                Master.Ack ack = expectMsgClass(Master.Ack.class);//assert ack
                Assert.assertEquals("trId1", ack.getWorkId());

                master.tell(new Master.UploadFile("trId2","/path","file","role","lib"), getRef());//send request
                ack = expectMsgClass(Master.Ack.class);//assert ack
                Assert.assertEquals("trId2", ack.getWorkId());

                master.tell(new MasterWorkerProtocol.WorkerRequestsFile("worker-testWorkerRequestsFile", "projectName", "127.0.0.1"), getRef());//send request
                final Master.FileJob fileJob = expectMsgClass(Master.FileJob.class);//assert ack
            }
        };
    }

    @Test
    public void testWorkerRequestsWork() {
        new TestKit(system) {
            {
                //submit job
                Master.Job job = getJob();
                master.tell(job, getRef());//send job
                expectMsgClass(Master.Ack.class);//assert ack
                //register worker
                master.tell(new MasterWorkerProtocol.RegisterWorker("worker-1"), getRef());
                expectMsgEquals(MasterWorkerProtocol.WorkIsReady.getInstance());
                //request work
                master.tell(new MasterWorkerProtocol.WorkerRequestsWork("worker-1", PROJECT_NAME), getRef());//send request
                final Master.Job job1 = expectMsgClass(Master.Job.class);//assert Job
                Assert.assertEquals(PROJECT_NAME, job1.roleId);
                //worker sends progress
                master.tell(new MasterWorkerProtocol.WorkInProgress("worker-1",job1.jobId), getRef());//send request
                expectNoMsg();
                //track job
                master.tell(new Master.TrackingInfo(job.trackingId), getRef());//send request
                TrackingResult trackingResult = expectMsgClass(TrackingResult.class);
                Assert.assertEquals(1,trackingResult.getInProgressCount());
                Assert.assertEquals(0,trackingResult.getPendingCount());
                //worker sends job completed msg
                master.tell(new MasterWorkerProtocol.WorkIsDone("worker-1", job1.jobId, new Worker.Result(1, "", "", null, job)), getRef());//send update
                expectMsgClass(Master.Ack.class);//assert ack
                //Check Idempotency works
                master.tell(new MasterWorkerProtocol.WorkIsDone("worker-1", job1.jobId, new Worker.Result(1, "", "", null, job)), getRef());//send update
                expectMsgClass(Master.Ack.class);//assert ack
                //track job after completed
                master.tell(new Master.TrackingInfo(job.trackingId), getRef());//send request
                trackingResult = expectMsgClass(TrackingResult.class);
                Assert.assertEquals(0,trackingResult.getInProgressCount());
                Assert.assertEquals(0, trackingResult.getPendingCount());
                Assert.assertEquals(false,trackingResult.isCancelled());
            }
        };
    }

    @Test
    public void testWorkCoordinationWhenJobFails() {
        new TestKit(system) {
            {
                //submit job
                Master.Job job = getJob();
                master.tell(job, getRef());//send job
                expectMsgClass(Master.Ack.class);//assert ack
                //register worker
                master.tell(new MasterWorkerProtocol.RegisterWorker("worker-2"), getRef());
                expectMsgEquals(MasterWorkerProtocol.WorkIsReady.getInstance());
                //request work
                master.tell(new MasterWorkerProtocol.WorkerRequestsWork("worker-2", PROJECT_NAME), getRef());//send request
                final Master.Job job1 = expectMsgClass(Master.Job.class);//assert Job
                Assert.assertEquals(PROJECT_NAME, job1.roleId);
                //worker sends progress
                master.tell(new MasterWorkerProtocol.WorkInProgress("worker-2",job1.jobId), getRef());//send request
                expectNoMsg();
                //track job
                master.tell(new Master.TrackingInfo(job.trackingId), getRef());//send request
                TrackingResult trackingResult = expectMsgClass(TrackingResult.class);
                Assert.assertEquals(1,trackingResult.getInProgressCount());
                Assert.assertEquals(0,trackingResult.getPendingCount());
                //worker sends job completed msg
                master.tell(new MasterWorkerProtocol.WorkFailed("worker-2", job1.jobId, new Worker.Result(1, "", "", null, job)), getRef());//send update
                //track job after completed
                ignoreNoMsg();
                master.tell(new Master.TrackingInfo(job.trackingId), getRef());//send request
                trackingResult = expectMsgClass(TrackingResult.class);
                Assert.assertEquals(0,trackingResult.getInProgressCount());
                Assert.assertEquals(0, trackingResult.getPendingCount());
                Assert.assertEquals(false,trackingResult.isCancelled());
                Assert.assertEquals(1,trackingResult.getFailed().size());
                Assert.assertEquals(0,trackingResult.getCompleted().size());
            }
        };
    }

    @Test
    public void testWorkerSendsFileUploadComplete() {
        new TestKit(system) {
            {
                master.tell(new Worker.FileUploadComplete(new Master.UploadFile("trId","/path","file","role","lib"),"127.0.0.1"), getRef());//send request
                expectNoMsg();
            }
        };
    }

}
