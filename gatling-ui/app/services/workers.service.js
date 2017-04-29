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
var Rx_1 = require('rxjs/Rx');
require('rxjs/add/operator/map');
var WorkerService = (function () {
    function WorkerService(_http, _jsonp) {
        this._http = _http;
        this._jsonp = _jsonp;
        this.baseUrl = "http://localhost:8080";
    }
    WorkerService.prototype.getWorkers = function () {
        return this._http.get(this.baseUrl + "/gatling/server/info", this.jsonHeaders())
            .map(this.extractData)
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
    WorkerService.prototype.handleError = function (error) {
        var errMsg = (error.message) ? error.message :
            error.status ? error.status + " - " + error.statusText : 'Server error';
        console.error('err' + errMsg); // log to console instead
        return Rx_1.Observable.throw(errMsg);
    };
    WorkerService = __decorate([
        core_1.Injectable(), 
        __metadata('design:paramtypes', [http_1.Http, http_1.Jsonp])
    ], WorkerService);
    return WorkerService;
}());
exports.WorkerService = WorkerService;
//# sourceMappingURL=workers.service.js.map