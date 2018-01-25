import {
    Component, OnInit, AfterViewInit, trigger, state, style, transition, animate, keyframes, OnDestroy
} from '@angular/core';
import {JobSummary} from '../../services/worker.metadata';
import {WorkerService} from '../../services/workers.service';
import { ActivatedRoute } from '@angular/router';
import { IntervalObservable } from 'rxjs/observable/IntervalObservable';

@Component({
    moduleId: module.id,
    selector: 'detail-cmp',
    templateUrl: 'detail.component.html',
    providers:[WorkerService]
})

export class DetailComponent implements OnInit, OnDestroy{
    private sub: any;
    public  jobSummary: JobSummary;
    private errorMessage: string;
    private trackingId: number;
    private reportInProgress: boolean;
    private reportResult: any;
    private cancelled: any;
    public cancelMsg: string = "Cancel Job >>"

    constructor(private workerService: WorkerService, private route: ActivatedRoute ){

    }
    ngOnInit(): void {
        this.sub = this.route.params.subscribe(params => {
            this.trackingId = params['trackingId'];
            // dispatch action to load the details here.
            console.log("The tracking id is: " + this.trackingId)
            this.fetchDetailData()

        });
        this.initializePolling()
        //console.log(this.pagedResult);

    }
    initializePolling() {

       var create =  IntervalObservable.create(30000);
       create.subscribe((x) => {
                //console.log(x)
           if(window.location.href.indexOf("detail") !== -1)
                return this.fetchDetailData();
        });
}

    ngOnDestroy(): void {
        this.sub.unsubscribe();
    }

    private fetchDetailData() {
        console.log("Fetching details for: " +  this.trackingId)
        this.workerService.getJobDetail(this.trackingId).subscribe(
            data => this.jobSummary = data,
            error => this.errorMessage = <any>error
        );
    }

    getLog(taskJobId: number, logType:string):string{
        return this.workerService.getBaseUrl() + "/gatling/server/getlog/" + this.trackingId + "/" + taskJobId + "/" + logType;
    }

    cancelReport():void{
       console.log("Canceling job: " +  this.trackingId);
        this.cancelMsg = "Cancelling Job ..."
        this.workerService.cancelJob(this.trackingId).subscribe(
            data => {
                console.log(data);
                this.cancelled = data.cancelled;
            },
            error => {
                this.errorMessage = <any>error
            }
        );
    }

    generateReport():void {
        this.reportInProgress = true;
        console.log("Generating job report: " +  this.trackingId);
        this.workerService.generateReport(this.trackingId).subscribe(
            data => {
                this.reportInProgress = false;
                console.log(data);
                this.reportResult = data.report;
            },
            error => {
                this.reportInProgress = false;
                this.errorMessage = <any>error
            }
        );
    }

    isSuccess( status:string): boolean{
       if(status=="COMPLETED")
           return true;
        return false
    }

    isFailed( status:string): boolean{
        if(status=="FAILED")
            return true;
        return false
    }
    isNormalStatus( status:string): boolean{
        if(status=="FAILED" || (status=="COMPLETED"))
            return false;
        return true
    }
}
