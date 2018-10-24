import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {RouterModule} from '@angular/router';
import {CommonModule, HashLocationStrategy, LocationStrategy} from '@angular/common';
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";

import {AppComponent} from './app.component';
import {DashboardComponent} from './dashboard/dashboard.component';

import {DashboardModule} from './dashboard/dashboard.module';
import {SidebarModule} from './sidebar/sidebar.module';
import {FooterModule} from './shared/footer/footer.module';
import {NavbarModule} from './shared/navbar/navbar.module';

import {AuthGuard} from './guard/auth.guard';

import {WorkerService} from "./services/workers.service";
import {ErrorInterceptor} from "./services/interceptor";

import { ChartistModule } from 'ng-chartist';

@NgModule({
    imports:      [
        BrowserModule,
        DashboardModule,
        SidebarModule,
        NavbarModule,
        FooterModule,
        CommonModule,
        HttpClientModule,
        RouterModule.forRoot([]),
        ChartistModule
    ],
    declarations: [ AppComponent, DashboardComponent ],
    providers: [
        AuthGuard,
        WorkerService,
        {
            provide: LocationStrategy,
            useClass: HashLocationStrategy},
        {
            provide: HTTP_INTERCEPTORS,
            useClass: ErrorInterceptor,
            multi: true
        }
    ],
    bootstrap:    [ AppComponent ]
})
export class AppModule { }
