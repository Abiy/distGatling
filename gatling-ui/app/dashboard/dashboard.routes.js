"use strict";
var home_component_1 = require('./home/home.component');
var run_component_1 = require('./run/run.component');
var running_component_1 = require('./running/running.component');
var completed_component_1 = require('./completed/completed.component');
var workers_component_1 = require('./workers/workers.component');
var master_component_1 = require('./master/master.component');
var detail_component_1 = require('./detail/detail.component');
exports.MODULE_ROUTES = [
    { path: '', redirectTo: 'overview', pathMatch: 'full' },
    { path: 'overview', component: home_component_1.HomeComponent },
    { path: 'running', component: running_component_1.RunningComponent },
    { path: 'completed', component: completed_component_1.CompletedComponent },
    { path: 'workers', component: workers_component_1.WorkersComponent },
    { path: 'master', component: master_component_1.MasterComponent },
    { path: 'run', component: run_component_1.RunComponent },
    { path: 'detail/:trackingId', component: detail_component_1.DetailComponent }
];
exports.MODULE_COMPONENTS = [
    home_component_1.HomeComponent,
    running_component_1.RunningComponent,
    completed_component_1.CompletedComponent,
    workers_component_1.WorkersComponent,
    master_component_1.MasterComponent,
    run_component_1.RunComponent,
    detail_component_1.DetailComponent
];
//# sourceMappingURL=dashboard.routes.js.map