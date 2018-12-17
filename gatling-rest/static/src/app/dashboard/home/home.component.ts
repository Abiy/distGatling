import {Component, OnInit } from '@angular/core';
import {WorkerService} from '../../services/workers.service';
import { ChartistModule } from 'ng-chartist';
import * as Chartist from "chartist";
import {IBarChartOptions, IResponsiveOptionTuple} from "chartist";

@Component({
    selector: 'home-cmp',
    templateUrl: 'home.component.html',
    providers:[WorkerService]
})

export class HomeComponent implements OnInit{
    private dashboardData: any;
    private errorMessage: any;

    constructor(private workerService: WorkerService){

    }

    ngOnInit() {
        this.fetchDashboardData();
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

    private initDashboard(dashboardData){

        var dataHost = {
            labels: dashboardData.host.keys,
            series: [ dashboardData.host.values ]
        };

        var optionsHost = {
            seriesBarDistance: 10,
            axisX: {
                showGrid: false
            },
            height: "245px",
            axisY: {
                onlyInteger: true,
            }
        };

        new Chartist.Bar('#chartHost', dataHost, optionsHost);

        var dataPartition = {
            labels: dashboardData.partition.keys,
            series: [ dashboardData.partition.values ]
        };

        var optionsPartition = {
            seriesBarDistance: 10,
            axisX: {
                showGrid: false
            },
            height: "245px"
        };

        new Chartist.Bar('#chartPartition', dataPartition, optionsPartition);


        var dataPartitionStatus = {
            labels: dashboardData.partitionStatus.keys,
            series: dashboardData.partitionStatus.values
        };

        var optionsPartitionStatus = {
            donut: true,
            donutWidth: 40,
            startAngle: 0,
            // total: 100,
            showLabel: true
        };

        new Chartist.Pie('#partitionStatus', dataPartitionStatus, optionsPartitionStatus);

        var dataStatus = {
            labels: dashboardData.status.keys,
            series: dashboardData.status.values
        };

        var optionsStatus = {
            donut: true,
            donutWidth: 40,
            startAngle: 0,
            // total: 100,
            showLabel: true
        };

        new Chartist.Pie('#status', dataStatus, optionsStatus);

    }

}
