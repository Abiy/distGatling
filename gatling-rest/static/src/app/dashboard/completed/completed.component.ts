import {Component, OnInit, AfterViewInit} from '@angular/core';
import {PagedResult} from '../../services/worker.metadata';
import {WorkerService} from '../../services/workers.service';
import { Router } from '@angular/router';


@Component({
    //moduleId: module.id,
    selector: 'table-completed',
    templateUrl: 'completed.component.html',
    providers: [WorkerService]
})


export class CompletedComponent implements OnInit {
    public pagedResult: PagedResult;
    private errorMessage: string;
    public currentPage: number = 1;

    constructor(private workerService: WorkerService, private router: Router) {

    }

    ngOnInit(): void {
        this.fetchData();
    }

    fetchData(): void {
        this.workerService.getCompleted(this.currentPage).subscribe(
            data => this.pagedResult = data,
            error => this.errorMessage = <any>error
        );
    }

    navigateToJobDetail(trackingId:string):void{
        this.router.navigate(["/detail/" + trackingId])
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

    onNext(): void{
        if(this.currentPage < this.pagedResult.totalPages ) {
            this.currentPage++;
            this.fetchData();
        }
    }

    onPrev(): void{
        if(this.currentPage > 1) {
            this.currentPage--;
            this.fetchData();
        }
    }

}
