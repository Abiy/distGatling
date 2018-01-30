import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import {Location, LocationStrategy, PathLocationStrategy} from '@angular/common';

@Component({
    selector: 'my-app',
    templateUrl: 'app/app.component.html'
})

export class AppComponent implements OnInit{
    location: Location;

    constructor(location:Location) {
        this.location = location;
    }
    ngOnInit(){
        $.getScript('../assets/js/gatling.js');
    }

    isLoggedIn(){
        return localStorage.getItem('currentUser') != null;
    }
}
