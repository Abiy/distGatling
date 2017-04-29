import { Route } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { RunComponent } from './run/run.component';
import { RunningComponent } from './running/running.component';
import { CompletedComponent } from './completed/completed.component';
import { WorkersComponent } from './workers/workers.component';
import { MasterComponent } from './master/master.component';


export const MODULE_ROUTES: Route[] =[
    { path: '', redirectTo: 'overview', pathMatch: 'full' },
    { path: 'overview', component: HomeComponent },
    { path: 'running', component: RunningComponent },
    { path: 'completed', component: CompletedComponent },
    { path: 'workers', component: WorkersComponent },
    { path: 'master', component: MasterComponent },
    { path: 'run', component: RunComponent }

]

export const MODULE_COMPONENTS = [
    HomeComponent,
    RunningComponent,
    CompletedComponent,
    WorkersComponent,
    MasterComponent,
    RunComponent
]
