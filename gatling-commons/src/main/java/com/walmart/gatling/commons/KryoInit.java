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

import com.esotericsoftware.kryo.Kryo;

/**
 * Created by walmart on 5/12/17.
 */
public class KryoInit {
    public void customize(Kryo kryo){



     //"com.walmart.gatling.commons.JobSummary.JobInfo"
     //"javafx.util.Pair"
     //"java.util.ArrayList"
     //"java.util.List"
     //"java.util.LinkedList"
       // com.walmart.gatling.commons.TaskEvent
       /* kryo.register(com.walmart.gatling.commons.MasterWorkerProtocol.RegisterWorker.class);
        kryo.register(com.walmart.gatling.commons.MasterWorkerProtocol.WorkerRequestsWork.class);
        kryo.register(com.walmart.gatling.commons.MasterWorkerProtocol.WorkFailed.class);
        kryo.register(com.walmart.gatling.commons.MasterWorkerProtocol.WorkIsDone.class);
        kryo.register(com.walmart.gatling.commons.MasterWorkerProtocol.WorkInProgress.class);
        kryo.register(com.walmart.gatling.commons.MasterWorkerProtocol.WorkIsReady.class);
        kryo.register(com.walmart.gatling.commons.MasterWorkerProtocol.WorkerRequestsFile.class);

        kryo.register(com.walmart.gatling.commons.Master.FileJob.class);
        kryo.register(com.walmart.gatling.commons.Master.Ack.class);
        kryo.register(com.walmart.gatling.commons.Master.GenerateReport.class);
        kryo.register(com.walmart.gatling.commons.Master.Job.class);
        kryo.register(com.walmart.gatling.commons.Master.JobSummaryInfo.class);
        kryo.register(com.walmart.gatling.commons.Master.Report.class);
        kryo.register(com.walmart.gatling.commons.Master.ServerInfo.class);
        kryo.register(com.walmart.gatling.commons.Master.TrackingInfo.class);
        kryo.register(com.walmart.gatling.commons.Master.UploadFile.class);
        kryo.register(com.walmart.gatling.commons.Master.UploadInfo.class);
        kryo.register(com.walmart.gatling.commons.Master.WorkerState.class);
        kryo.register(com.walmart.gatling.commons.Master.WorkerStatus.class);


        kryo.register(com.walmart.gatling.commons.JobState.JobAccepted.class);
        kryo.register(com.walmart.gatling.commons.JobState.JobCompleted.class);
        kryo.register(com.walmart.gatling.commons.JobState.JobFailed.class);
        kryo.register(com.walmart.gatling.commons.JobState.JobPending.class);
        kryo.register(com.walmart.gatling.commons.JobState.JobPostponed.class);
        kryo.register(com.walmart.gatling.commons.JobState.JobStarted.class);
        kryo.register(com.walmart.gatling.commons.JobState.JobStatusString.class);
        kryo.register(com.walmart.gatling.commons.JobState.JobTimedOut.class);
*/


    }
}
