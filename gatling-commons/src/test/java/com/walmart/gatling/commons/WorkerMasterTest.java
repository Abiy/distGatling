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

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

import akka.testkit.JavaTestKit;

/**
 * Created by walmart
 */
public class WorkerMasterTest extends MasterTest {

    public static final String PROJECT_NAME = "projectName";

    @Test
    public void testSubmitJobAndRegisterWorker() {
        new JavaTestKit(system) {
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
        new JavaTestKit(system) {
            {
                master.tell(new MasterWorkerProtocol.WorkerRequestsFile("worker-testWorkerRequestsFile","projectName","127.0.0.1"), getRef());//send request
                expectNoMsg();
                master.tell(new Master.UploadFile("trId","/path","file","role","lib"), getRef());//send request
                final Master.Ack ack = expectMsgClass(Master.Ack.class);//assert ack
                Assert.assertEquals("trId", ack.getWorkId());
                master.tell(new MasterWorkerProtocol.WorkerRequestsFile("worker-testWorkerRequestsFile", "projectName", "127.0.0.1"), getRef());//send request
                final Master.FileJob fileJob = expectMsgClass(Master.FileJob.class);//assert ack
            }
        };
    }

    @Test
    public void testWorkerRequestsWork() {
        new JavaTestKit(system) {
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
            }
        };
    }
}
