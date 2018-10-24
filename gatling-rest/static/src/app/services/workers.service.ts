/**
 * Created by ahailem on 4/27/17.
 */

import { Injectable } from '@angular/core';
import {Worker, PagedResult, JobSummary, TaskEvent, JobInfo} from "./worker.metadata";
import {HttpClient, HttpHeaders} from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import { environment } from '../../environments/environment';
import {Router} from "@angular/router";

@Injectable({
    providedIn: 'root'
})
export class WorkerService {
    private baseUrl = environment.apiUrl;

    getBaseUrl(): string{
        return this.baseUrl;
    }

    constructor(private http: HttpClient){ }
  
    getWorkers(): Observable<Worker[]>{
      return this.http.get<Worker[]>( this.baseUrl + "/gatling/server/info", { headers: this.headers() })
    }

    getRunning(pageNum:number): Observable<PagedResult>{
        return this.http.get<PagedResult>( this.baseUrl + "/gatling/server/running/summary?size=15&page=" + pageNum, { headers: this.headers() })
    }

    getCompleted(pageNum:number): Observable<PagedResult>{
        return this.http.get<PagedResult>( this.baseUrl + "/gatling/server/completed/summary?size=15&page=" + pageNum, { headers: this.headers() })
    }

    getJobDetail(trackingId: number): Observable<JobSummary>{
       return this.http.get<JobSummary>( this.baseUrl + "/gatling/server/detail/" + trackingId, { headers: this.headers() })
    }


    cancelJob(trackingId: number): Observable<any>{
        return this.http.post( this.baseUrl + "/gatling/server/abort/" + trackingId , "", { headers: this.headers() })
    }


    generateReport(trackingId: number): Observable<any>{
        return this.http.post( this.baseUrl + "/gatling/server/report/" + trackingId , "", { headers: this.headers() })
    }

    getMasterMetrics(): Observable<any>{
        return this.http.get<any>( this.baseUrl + "/metrics", { headers: this.headers() })

    }

    getDashboardData(): Observable<any> {
        return this.http.get<any>( this.baseUrl + "/gatling/server/dashboard", { headers: this.headers() })
    }


    public headers(): HttpHeaders {
        return new HttpHeaders()
            .set('Content-Type', 'application/json; charset=utf-8')
            .set("Cache-Control", "no-cache")
            .set("Cache-Control", "no-store")
            .set("Authorization", localStorage.getItem('currentUser'))
    }

}
