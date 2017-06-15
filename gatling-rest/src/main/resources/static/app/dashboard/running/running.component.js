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
var RunningComponent = (function () {
    function RunningComponent(workerService, router) {
        this.workerService = workerService;
        this.router = router;
        this.currentPage = 1;
    }
    RunningComponent.prototype.ngOnInit = function () {
        this.fetchData();
    };
    RunningComponent.prototype.fetchData = function () {
        var _this = this;
        this.workerService.getRunning(this.currentPage).subscribe(function (data) { return _this.pagedResult = data; }, function (error) { return _this.errorMessage = error; });
    };
    RunningComponent.prototype.navigateToJobDetail = function (trackingId) {
        this.router.navigate(["/detail/" + trackingId]);
    };
    RunningComponent.prototype.isSuccess = function (status) {
        if (status == "COMPLETED")
            return true;
        return false;
    };
    RunningComponent.prototype.isFailed = function (status) {
        if (status == "FAILED")
            return true;
        return false;
    };
    RunningComponent.prototype.isNormalStatus = function (status) {
        if (status == "FAILED" || (status == "COMPLETED"))
            return false;
        return true;
    };
    RunningComponent.prototype.onNext = function () {
        if (this.currentPage < this.pagedResult.totalPages) {
            this.currentPage++;
            this.fetchData();
        }
    };
    RunningComponent.prototype.onPrev = function () {
        if (this.currentPage > 1) {
            this.currentPage--;
            this.fetchData();
        }
    };
    RunningComponent = __decorate([
        core_1.Component({
            moduleId: module.id,
            selector: 'table-running',
            templateUrl: 'running.component.html',
            providers: [workers_service_1.WorkerService]
        }), 
        __metadata('design:paramtypes', [workers_service_1.WorkerService, router_1.Router])
    ], RunningComponent);
    return RunningComponent;
}());
exports.RunningComponent = RunningComponent;
//# sourceMappingURL=running.component.js.map