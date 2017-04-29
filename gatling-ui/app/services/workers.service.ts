/**
 * Created by ahailem on 4/27/17.
 */

import { Injectable } from '@angular/core';
import {Worker} from "./worker.metadata";
import { Http, Response, Headers , Jsonp,} from '@angular/http';
import { Observable } from 'rxjs/Rx';
import 'rxjs/add/operator/map';


@Injectable()
export class WorkerService {
    private baseUrl: string;

    constructor(private _http: Http,private _jsonp: Jsonp){
       this.baseUrl = "http://localhost:8080";
    }

    getWorkers(): Observable<Worker[]>{
      return this._http.get( this.baseUrl + "/gatling/server/info",this.jsonHeaders())
            .map(this.extractData)
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

    private handleError (error: any) {
        let errMsg = (error.message) ? error.message :
            error.status ? `${error.status} - ${error.statusText}` : 'Server error';
        console.error('err' + errMsg); // log to console instead
        return Observable.throw(errMsg);
    }
}
