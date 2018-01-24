"use strict";
var home_component_1 = require('./home/home.component');
var run_component_1 = require('./run/run.component');
var running_component_1 = require('./running/running.component');
var completed_component_1 = require('./completed/completed.component');
var workers_component_1 = require('./workers/workers.component');
var master_component_1 = require('./master/master.component');
var detail_component_1 = require('./detail/detail.component');
var login_component_1 = require('./login/login.component');
var auth_guard_1 = require('../guard/auth.guard');
exports.MODULE_ROUTES = [
    { path: '', redirectTo: 'overview', pathMatch: 'full' },
    { path: 'overview', component: home_component_1.HomeComponent, canActivate: [auth_guard_1.AuthGuard] },
    { path: 'running', component: running_component_1.RunningComponent, canActivate: [auth_guard_1.AuthGuard] },
    { path: 'completed', component: completed_component_1.CompletedComponent, canActivate: [auth_guard_1.AuthGuard] },
    { path: 'workers', component: workers_component_1.WorkersComponent, canActivate: [auth_guard_1.AuthGuard] },
    { path: 'master', component: master_component_1.MasterComponent, canActivate: [auth_guard_1.AuthGuard] },
    { path: 'run', component: run_component_1.RunComponent, canActivate: [auth_guard_1.AuthGuard] },
    { path: 'login', component: login_component_1.LoginComponent },
    { path: 'detail/:trackingId', component: detail_component_1.DetailComponent, canActivate: [auth_guard_1.AuthGuard] }
];
exports.MODULE_COMPONENTS = [
    home_component_1.HomeComponent,
    running_component_1.RunningComponent,
    completed_component_1.CompletedComponent,
    workers_component_1.WorkersComponent,
    master_component_1.MasterComponent,
    run_component_1.RunComponent,
    detail_component_1.DetailComponent,
    login_component_1.LoginComponent
];
//# sourceMappingURL=dashboard.routes.js.map