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
