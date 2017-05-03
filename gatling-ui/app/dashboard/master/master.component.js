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
var MasterComponent = (function () {
    function MasterComponent(workerService, router) {
        this.workerService = workerService;
        this.router = router;
    }
    MasterComponent.prototype.ngOnInit = function () {
        this.fetchData();
    };
    MasterComponent.prototype.fetchData = function () {
        var _this = this;
        this.workerService.getMasterMetrics().subscribe(function (data) { return _this.metrics = _this.transform(data); }, function (error) { return _this.errorMessage = error; });
    };
    MasterComponent.prototype.transform = function (dict) {
        var a = [];
        for (var key in dict) {
            if (dict.hasOwnProperty(key)) {
                a.push({ key: key, val: dict[key] });
            }
        }
        return a;
    };
    MasterComponent.prototype.getMemoryMetrics = function (input) {
        var result = [];
        for (var _i = 0, input_1 = input; _i < input_1.length; _i++) {
            var entry = input_1[_i];
            if (entry.key.startsWith("memory.heap."))
                result.push(entry);
        }
        return result;
    };
    MasterComponent.prototype.getThreadMetrics = function (input) {
        var result = [];
        for (var _i = 0, input_2 = input; _i < input_2.length; _i++) {
            var entry = input_2[_i];
            if (entry.key.startsWith("threads."))
                result.push(entry);
        }
        return result;
    };
    MasterComponent.prototype.getGcMetrics = function (input) {
        var result = [];
        for (var _i = 0, input_3 = input; _i < input_3.length; _i++) {
            var entry = input_3[_i];
            if (entry.key.startsWith("gc."))
                result.push(entry);
        }
        return result;
    };
    MasterComponent = __decorate([
        core_1.Component({
            moduleId: module.id,
            selector: 'home-cmp',
            templateUrl: 'master.component.html',
            providers: [workers_service_1.WorkerService]
        }), 
        __metadata('design:paramtypes', [workers_service_1.WorkerService, router_1.Router])
    ], MasterComponent);
    return MasterComponent;
}());
exports.MasterComponent = MasterComponent;
//# sourceMappingURL=master.component.js.map