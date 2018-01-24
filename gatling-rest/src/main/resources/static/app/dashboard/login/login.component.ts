import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';

//import { AlertService, AuthenticationService } from '../_services/index';

@Component({
    moduleId: module.id,
    templateUrl: 'login.component.html'
})

export class LoginComponent implements OnInit {
    model: any = {};
    loading = false;
    returnUrl: string;
    cause: string;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        //private authenticationService: AuthenticationService,
        //private alertService: AlertService
    ) { }

    ngOnInit() {
        // reset login status
        //this.authenticationService.logout();
        localStorage.removeItem('currentUser');

        // get return url from route parameters or default to '/'
        this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
        this.cause = this.route.snapshot.queryParams['cause'] || ''
    }

    login() {
        this.loading = true;
        let currentUser = "Basic " + btoa(this.model.username + ":" + this.model.password);
        localStorage.setItem('currentUser', currentUser);
        this.router.navigate([this.returnUrl]);
        //this.authenticationService.login(this.model.username, this.model.password)
        //    .subscribe(
        //        data => {
        //            this.router.navigate([this.returnUrl]);
        //        },
        //        error => {
        //            this.alertService.error(error);
        //            this.loading = false;
        //        });
    }
}
