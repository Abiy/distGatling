import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';
import {HttpModule, Http, XHRBackend, RequestOptions} from '@angular/http';
import { CommonModule } from '@angular/common';
//import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';

//import { JwtInterceptor } from './services/jwt.interceptor';
import {httpFactory} from "./services/http.factory";

import { AppComponent }   from './app.component';
import { DashboardComponent } from './dashboard/dashboard.component';

import { DashboardModule } from './dashboard/dashboard.module';

import { SidebarModule } from './sidebar/sidebar.module';
import { FooterModule } from './shared/footer/footer.module';
import { NavbarModule} from './shared/navbar/navbar.module';

import { AuthGuard } from './guard/auth.guard';

import { HashLocationStrategy, LocationStrategy } from '@angular/common';

@NgModule({
    imports:      [
        BrowserModule,
        DashboardModule,
        SidebarModule,
        NavbarModule,
        FooterModule,
        CommonModule,
        HttpModule,
        RouterModule.forRoot([])
    ],
    declarations: [ AppComponent, DashboardComponent ],
    providers: [
        AuthGuard,
        {provide: LocationStrategy, useClass: HashLocationStrategy},
        //{provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true}
        {
            provide: Http,
            useFactory: httpFactory,
            deps: [XHRBackend, RequestOptions]
        }
    ],
    bootstrap:    [ AppComponent ]
})
export class AppModule { }
