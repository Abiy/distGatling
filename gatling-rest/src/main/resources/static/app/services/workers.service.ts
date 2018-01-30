/**
 * Created by ahailem on 4/27/17.
 */

import { Injectable } from '@angular/core';
import {Worker, PagedResult, JobSummary, TaskEvent, JobInfo} from "./worker.metadata";
import { Http, Response, Headers , Jsonp,} from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import { environment } from '../environments/environment';
import { Router } from '@angular/router';

@Injectable()
export class WorkerService {
    private baseUrl: string;

    getBaseUrl(): string{
        return this.baseUrl;
    }

    constructor(private _http: Http,private _jsonp: Jsonp, private _router: Router){
       this.baseUrl = environment.apiUrl;
    }
  
    getWorkers(): Observable<Worker[]>{
      return this._http.get( this.baseUrl + "/gatling/server/info",this.jsonHeaders())
            .map(this.extractData)
            .catch(this.handleError.bind(this))
    }

    getRunning(pageNum:number): Observable<PagedResult>{
        return this._http.get( this.baseUrl + "/gatling/server/running/summary?size=15&page="+pageNum,this.jsonHeaders())
            .map(this.extractPagedResult)
            .catch(this.handleError.bind(this))
    }

    getCompleted(pageNum:number): Observable<PagedResult>{
        return this._http.get( this.baseUrl + "/gatling/server/completed/summary?size=15&page="+pageNum,this.jsonHeaders())
            .map(this.extractPagedResult)
            .catch(this.handleError.bind(this))
    }

    getJobDetail(trackingId: number): Observable<any>{
       return this._http.get( this.baseUrl + "/gatling/server/detail/"+trackingId ,this.jsonHeaders())
        .map(this.extractJobDetailResult)
        .catch(this.handleError.bind(this))
    }


    cancelJob(trackingId: number): Observable<any>{
        return this._http.post( this.baseUrl + "/gatling/server/abort/"+trackingId ,this.jsonHeaders())
            .map(this.extractJobDetailResult)
            .catch(this.handleError.bind(this))
    }


    generateReport(trackingId: number): Observable<any>{
        return this._http.post( this.baseUrl + "/gatling/server/report/"+trackingId ,this.jsonHeaders())
            .map(this.extractJobDetailResult)
            .catch(this.handleError.bind(this))
    }

    getMasterMetrics(): Observable<any>{
        return this._http.get( this.baseUrl + "/metrics" ,this.jsonHeaders())
            .map(this.extractPagedResult)
            .catch(this.handleError.bind(this))
    }

    getDashboardData(): Observable<any> {
        return this._http.get( this.baseUrl + "/gatling/server/dashboard" ,this.jsonHeaders())
            .map(this.extractPagedResult)
            .catch(this.handleError.bind(this))
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
            const body = error.status != 404 ? error.json() : '';
            const err = body.error || JSON.stringify(body);
            if (error.status == 0) {
                errMsg = `${error.status} - No response from server`;    
            } else {
                errMsg = `${error.status} - ${err}`;
            }
        } else {
            errMsg = error.message ? error.message : error.toString();
        }
        console.error(errMsg);

        if (error.status == 401 || error.status == 0 || error.status == 404) {
            this._router.navigate(['/login'], { queryParams: { cause: errMsg }});
            return;
        } 

        return Observable.throw(errMsg);
    }

}
