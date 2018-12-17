import { Component, OnInit } from '@angular/core';
import { ROUTES } from './sidebar-routes.config';

@Component({
    selector: 'sidebar-cmp',
    templateUrl: 'sidebar.component.html',
})

export class SidebarComponent implements OnInit {
    public menuItems: any[];

    constructor() {}
    ngOnInit() {
        this.menuItems = ROUTES.filter(menuItem => menuItem);
    }
}
