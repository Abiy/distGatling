import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import {Location, LocationStrategy, PathLocationStrategy} from '@angular/common';

@Component({
    selector: 'my-app',
    templateUrl: 'app.component.html'
})

export class AppComponent implements OnInit{
    location: Location;

    constructor(location:Location) {
        this.location = location;
    }
    ngOnInit(){
    }

    isLoggedIn(){
        return localStorage.getItem('currentUser') != null;
    }
}
