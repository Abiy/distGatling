
import {Pipe} from "@angular/core";
export interface Worker {
    actor: string;
    host: string;
    status: string;
    workerId: string;
}

export interface JobInfo {
    partitionAccessKey: string;
    user: string;
    partitionName: string;
    jobName: string;
    trackingId: string;
    count: number;

}

export interface TaskEvent{
    jobName: string;
    startTimeStamp: number;
    endTimeStamp: number;
    workerId: string;
    errorLogPath: string;
    stdLogPath: string
    status: string;
    taskJobId: string;
    parameters: { key:string; value:string };
    jobInfo: JobInfo;

}

export interface JobSummary {
    taskInfoList: Array<TaskEvent>;
    jobInfo: JobInfo;
    endTime: number;
    startTime: number;
    status: string;
}

export interface PagedResult {
    last: boolean;
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
    sort: string;
    first: boolean;
    numberOfElements: number;
    content: Array<JobSummary>;
}

export interface KeyVal {
    key:string,
    val:number
}

export class SimulationModel{
    constructor(public packageName:string,
                public partitionName:string,
                public tag: string,
                public userName: string,
                public accessKey: string,
                public parallelism: number
    ){}

}
