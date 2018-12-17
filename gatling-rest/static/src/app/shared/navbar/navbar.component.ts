import { Component, OnInit } from '@angular/core';
import { ROUTES } from '../../sidebar/sidebar-routes.config';
import {Location, LocationStrategy, PathLocationStrategy} from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
    selector: 'navbar-cmp',
    templateUrl: 'navbar.component.html'
})

export class NavbarComponent implements OnInit{
    private listTitles: any[];
    location: Location;
    constructor(location:Location, private router: Router) {
        this.location = location;
    }
    ngOnInit(){
        this.listTitles = ROUTES.filter(listTitle => listTitle);
    }
    getTitle(){
        var titlee = this.location.prepareExternalUrl(this.location.path());
        if(titlee.charAt(0) === '#'){
            titlee = titlee.slice( 2 );
        }
        for(var item = 0; item < this.listTitles.length; item++){
            if(this.listTitles[item].path === titlee){
                return this.listTitles[item].title;
            }
        }
        return 'Cluster';
    }

    logout(){
        localStorage.removeItem('currentUser');
        this.router.navigate(["/login"]);
    }
}
