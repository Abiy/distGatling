/**
 * Created by ahailem on 4/27/17.
 */

import { Injectable } from '@angular/core';
import {Worker, PagedResult, JobSummary, TaskEvent, JobInfo} from "./worker.metadata";
import { Http, Response, Headers , Jsonp,} from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';


@Injectable()
export class WorkerService {
    private baseUrl: string;

    constructor(private _http: Http,private _jsonp: Jsonp){
       this.baseUrl = "";
    }

    getWorkers(): Observable<Worker[]>{
      return this._http.get( this.baseUrl + "/gatling/server/info",this.jsonHeaders())
            .map(this.extractData)
            .catch(this.handleError)
    }

    getRunning(pageNum:number): Observable<PagedResult>{
        return this._http.get( this.baseUrl + "/gatling/server/running/summary?size=15&page="+pageNum,this.jsonHeaders())
            .map(this.extractPagedResult)
            .catch(this.handleError)
    }

    getCompleted(pageNum:number): Observable<PagedResult>{
        return this._http.get( this.baseUrl + "/gatling/server/completed/summary?size=15&page="+pageNum,this.jsonHeaders())
            .map(this.extractPagedResult)
            .catch(this.handleError)
    }

    getJobDetail(trackingId: number): Observable<any>{
       return this._http.get( this.baseUrl + "/gatling/server/detail/"+trackingId ,this.jsonHeaders())
        .map(this.extractJobDetailResult)
        .catch(this.handleError)
    }


    cancelJob(trackingId: number): Observable<any>{
        return this._http.post( this.baseUrl + "/gatling/server/abort/"+trackingId ,this.jsonHeaders())
            .map(this.extractJobDetailResult)
            .catch(this.handleError)
    }


    generateReport(trackingId: number): Observable<any>{
        return this._http.post( this.baseUrl + "/gatling/server/report/"+trackingId ,this.jsonHeaders())
            .map(this.extractJobDetailResult)
            .catch(this.handleError)
    }

    getMasterMetrics(): Observable<any>{
        return this._http.get( this.baseUrl + "/metrics" ,this.jsonHeaders())
            .map(this.extractPagedResult)
            .catch(this.handleError)
    }

    getDashboardData(): Observable<any> {
        return this._http.get( this.baseUrl + "/gatling/server/dashboard" ,this.jsonHeaders())
            .map(this.extractPagedResult)
            .catch(this.handleError)
    }




    public jsonHeaders(): Headers {
        let headers: Headers = new Headers();
        headers.append('Content-Type', 'application/json; charset=utf-8');
        headers.append("Cache-Control", "no-cache");
        headers.append("Cache-Control", "no-store");
        headers.append("If-Modified-Since", "Mon, 26 Jul 1997 05:00:00 GMT");

        return headers;
    }

    private extractData(res: Response) {
        console.log('extracting data');
        return <Worker[]>res.json() ;
    }

    private extractPagedResult(res: Response) {
        let body = res.json();
        return body || {};
    }

    private extractJobDetailResult(res: Response) {
        let body = res.json();
        return body || {};
    }

    private handleError (error: Response | any) {
        // In a real world app, you might use a remote logging infrastructure
        let errMsg: string;
        if (error instanceof Response) {
            const body = error.json() || '';
            const err = body.error || JSON.stringify(body);
            errMsg = `${error.status} - ${error.statusText || ''} ${err}`;
        } else {
            errMsg = error.message ? error.message : error.toString();
        }
        console.error(errMsg);
        return Observable.throw(errMsg);
    }

}
