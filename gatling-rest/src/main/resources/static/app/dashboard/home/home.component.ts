import {Component, OnInit,trigger,state,style,transition,animate,keyframes, group} from '@angular/core';
import initDemo = require('../../../assets/js/charts.js');
import initNotify = require('../../../assets/js/notify.js');
import {WorkerService} from '../../services/workers.service';

declare var $:any;

@Component({
    moduleId: module.id,
    selector: 'home-cmp',
    templateUrl: 'home.component.html',
    providers:[WorkerService]
})

export class HomeComponent implements OnInit{
    private dashboardData: any;
    private errorMessage: any;

    constructor(private workerService: WorkerService,){

    }

    ngOnInit() {
        // $.getScript('../../../assets/js/bootstrap-checkbox-radio-switch.js');
        // $.getScript('../../../assets/js/gatling.js');

        $('[data-toggle="checkbox"]').each(function () {
            if($(this).data('toggle') == 'switch') return;

            var $checkbox = $(this);
            $checkbox.checkbox();
        });
        this.fetchDashboardData();
        //initNotify();
    }

    private fetchDashboardData() {
        this.workerService.getDashboardData().subscribe(
            data => {
                this.dashboardData = data;
                this.initDashboard(data);
            },
            error => this.errorMessage = <any>error
        );
    }

    private initDashboard(data){
        initDemo(data);
    }
}
