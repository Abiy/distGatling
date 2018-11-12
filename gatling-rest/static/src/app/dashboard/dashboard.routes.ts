import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { RunComponent } from './run/run.component';
import { RunningComponent } from './running/running.component';
import { CompletedComponent } from './completed/completed.component';
import { WorkersComponent } from './workers/workers.component';
import { MasterComponent } from './master/master.component';
import { DetailComponent } from './detail/detail.component';
import { LoginComponent } from './login/login.component';

import { AuthGuard } from '../guard/auth.guard';

export const MODULE_ROUTES: Routes =[
    { path: '', redirectTo: 'overview', pathMatch: 'full', canActivate: [AuthGuard] },
    { path: 'overview', component: HomeComponent, canActivate: [AuthGuard] },
    { path: 'running', component: RunningComponent, canActivate: [AuthGuard] },
    { path: 'completed', component: CompletedComponent, canActivate: [AuthGuard] },
    { path: 'workers', component: WorkersComponent, canActivate: [AuthGuard] },
    { path: 'master', component: MasterComponent, canActivate: [AuthGuard] },
    { path: 'run', component: RunComponent, canActivate: [AuthGuard] },
    { path: 'login', component: LoginComponent },
    { path: 'detail/:trackingId', component: DetailComponent, canActivate: [AuthGuard] }

];

export const MODULE_COMPONENTS = [
    HomeComponent,
    RunningComponent,
    CompletedComponent,
    WorkersComponent,
    MasterComponent,
    RunComponent,
    DetailComponent,
    LoginComponent
];
