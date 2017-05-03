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
var workers_service_1 = require('../../services/workers.service');
var router_1 = require('@angular/router');
var IntervalObservable_1 = require('rxjs/observable/IntervalObservable');
var DetailComponent = (function () {
    function DetailComponent(workerService, route) {
        this.workerService = workerService;
        this.route = route;
    }
    DetailComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.sub = this.route.params.subscribe(function (params) {
            _this.trackingId = params['trackingId'];
            // dispatch action to load the details here.
            console.log("The tracking id is: " + _this.trackingId);
            _this.fetchDetailData();
        });
        this.initializePolling();
        //console.log(this.pagedResult);
    };
    DetailComponent.prototype.initializePolling = function () {
        var _this = this;
        var create = IntervalObservable_1.IntervalObservable.create(30000);
        create.subscribe(function (x) {
            //console.log(x)
            return _this.fetchDetailData();
        });
    };
    DetailComponent.prototype.ngOnDestroy = function () {
        this.sub.unsubscribe();
    };
    DetailComponent.prototype.fetchDetailData = function () {
        var _this = this;
        console.log("Fetching details for: " + this.trackingId);
        this.workerService.getJobDetail(this.trackingId).subscribe(function (data) { return _this.jobSummary = data; }, function (error) { return _this.errorMessage = error; });
    };
    DetailComponent.prototype.isSuccess = function (status) {
        if (status == "COMPLETED")
            return true;
        return false;
    };
    DetailComponent.prototype.isFailed = function (status) {
        if (status == "FAILED")
            return true;
        return false;
    };
    DetailComponent.prototype.isNormalStatus = function (status) {
        if (status == "FAILED" || (status == "COMPLETED"))
            return false;
        return true;
    };
    DetailComponent = __decorate([
        core_1.Component({
            moduleId: module.id,
            selector: 'detail-cmp',
            templateUrl: 'detail.component.html',
            providers: [workers_service_1.WorkerService]
        }), 
        __metadata('design:paramtypes', [workers_service_1.WorkerService, router_1.ActivatedRoute])
    ], DetailComponent);
    return DetailComponent;
}());
exports.DetailComponent = DetailComponent;
//# sourceMappingURL=detail.component.js.map