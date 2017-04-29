import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MODULE_COMPONENTS, MODULE_ROUTES } from './dashboard.routes';
import { HttpModule } from '@angular/http';
import { CommonModule } from '@angular/common';
import {JsonpModule} from '@angular/http';

@NgModule({
    imports: [
        JsonpModule,
        HttpModule,
        CommonModule,
        RouterModule.forChild(MODULE_ROUTES)
    ],
    declarations: [ MODULE_COMPONENTS ]
})

export class DashboardModule{}
