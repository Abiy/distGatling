/**
 * Created by ahailem on 4/27/17.
 */
"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var core_1 = require('@angular/core');
var http_1 = require('@angular/http');
var Observable_1 = require('rxjs/Observable');
require('rxjs/add/operator/catch');
require('rxjs/add/operator/map');
var WorkerService = (function () {
    function WorkerService(_http, _jsonp) {
        this._http = _http;
        this._jsonp = _jsonp;
        //this.baseUrl = "http://localhost:8080";
        this.baseUrl = "";
    }
    WorkerService.prototype.getWorkers = function () {
        return this._http.get(this.baseUrl + "/gatling/server/info", this.jsonHeaders())
            .map(this.extractData)
            .catch(this.handleError);
    };
    WorkerService.prototype.getRunning = function (pageNum) {
        return this._http.get(this.baseUrl + "/gatling/server/running/summary?size=15&page=" + pageNum, this.jsonHeaders())
            .map(this.extractPagedResult)
            .catch(this.handleError);
    };
    WorkerService.prototype.getCompleted = function (pageNum) {
        return this._http.get(this.baseUrl + "/gatling/server/completed/summary?size=15&page=" + pageNum, this.jsonHeaders())
            .map(this.extractPagedResult)
            .catch(this.handleError);
    };
    WorkerService.prototype.getJobDetail = function (trackingId) {
        return this._http.get(this.baseUrl + "/gatling/server/detail/" + trackingId, this.jsonHeaders())
            .map(this.extractJobDetailResult)
            .catch(this.handleError);
    };
    WorkerService.prototype.cancelJob = function (trackingId) {
        return this._http.post(this.baseUrl + "/gatling/server/abort/" + trackingId, this.jsonHeaders())
            .map(this.extractJobDetailResult)
            .catch(this.handleError);
    };
    WorkerService.prototype.generateReport = function (trackingId) {
        return this._http.post(this.baseUrl + "/gatling/server/report/" + trackingId, this.jsonHeaders())
            .map(this.extractJobDetailResult)
            .catch(this.handleError);
    };
    WorkerService.prototype.getMasterMetrics = function () {
        return this._http.get(this.baseUrl + "/metrics", this.jsonHeaders())
            .map(this.extractPagedResult)
            .catch(this.handleError);
    };
    WorkerService.prototype.getDashboardData = function () {
        return this._http.get(this.baseUrl + "/gatling/server/dashboard", this.jsonHeaders())
            .map(this.extractPagedResult)
            .catch(this.handleError);
    };
    WorkerService.prototype.jsonHeaders = function () {
        var headers = new http_1.Headers();
        headers.append('Content-Type', 'application/json; charset=utf-8');
        headers.append("Cache-Control", "no-cache");
        headers.append("Cache-Control", "no-store");
        headers.append("If-Modified-Since", "Mon, 26 Jul 1997 05:00:00 GMT");
        return headers;
    };
    WorkerService.prototype.extractData = function (res) {
        console.log('extracting data');
        return res.json();
    };
    WorkerService.prototype.extractPagedResult = function (res) {
        var body = res.json();
        return body || {};
    };
    WorkerService.prototype.extractJobDetailResult = function (res) {
        var body = res.json();
        return body || {};
    };
    WorkerService.prototype.handleError = function (error) {
        // In a real world app, you might use a remote logging infrastructure
        var errMsg;
        if (error instanceof http_1.Response) {
            var body = error.json() || '';
            var err = body.error || JSON.stringify(body);
            errMsg = error.status + " - " + (error.statusText || '') + " " + err;
        }
        else {
            errMsg = error.message ? error.message : error.toString();
        }
        console.error(errMsg);
        return Observable_1.Observable.throw(errMsg);
    };
    WorkerService = __decorate([
        core_1.Injectable(), 
        __metadata('design:paramtypes', [http_1.Http, http_1.Jsonp])
    ], WorkerService);
    return WorkerService;
}());
exports.WorkerService = WorkerService;
//# sourceMappingURL=workers.service.js.map