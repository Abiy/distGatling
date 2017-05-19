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
var WorkersComponent = (function () {
    function WorkersComponent(workerService) {
        this.workerService = workerService;
        //this.workers = workerService.getWorkers();
    }
    WorkersComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.workerService.getWorkers().subscribe(function (data) { return _this.workers = data; }, function (error) { return _this.errorMessage = error; });
        console.log(this.workers);
    };
    WorkersComponent = __decorate([
        core_1.Component({
            moduleId: module.id,
            selector: 'table-cmp',
            templateUrl: 'workers.component.html',
            providers: [workers_service_1.WorkerService]
        }), 
        __metadata('design:paramtypes', [workers_service_1.WorkerService])
    ], WorkersComponent);
    return WorkersComponent;
}());
exports.WorkersComponent = WorkersComponent;
//# sourceMappingURL=workers.component.js.map