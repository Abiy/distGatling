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
var initDemo = require('../../../assets/js/charts.js');
var workers_service_1 = require('../../services/workers.service');
var HomeComponent = (function () {
    function HomeComponent(workerService) {
        this.workerService = workerService;
    }
    HomeComponent.prototype.ngOnInit = function () {
        // $.getScript('../../../assets/js/bootstrap-checkbox-radio-switch.js');
        // $.getScript('../../../assets/js/gatling.js');
        $('[data-toggle="checkbox"]').each(function () {
            if ($(this).data('toggle') == 'switch')
                return;
            var $checkbox = $(this);
            $checkbox.checkbox();
        });
        this.fetchDashboardData();
        //initNotify();
    };
    HomeComponent.prototype.fetchDashboardData = function () {
        var _this = this;
        this.workerService.getDashboardData().subscribe(function (data) {
            _this.dashboardData = data;
            _this.initDashboard(data);
        }, function (error) { return _this.errorMessage = error; });
    };
    HomeComponent.prototype.initDashboard = function (data) {
        initDemo(data);
    };
    HomeComponent = __decorate([
        core_1.Component({
            moduleId: module.id,
            selector: 'home-cmp',
            templateUrl: 'home.component.html',
            providers: [workers_service_1.WorkerService]
        }), 
        __metadata('design:paramtypes', [workers_service_1.WorkerService])
    ], HomeComponent);
    return HomeComponent;
}());
exports.HomeComponent = HomeComponent;
//# sourceMappingURL=home.component.js.map