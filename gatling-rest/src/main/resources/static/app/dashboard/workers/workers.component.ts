import {Component, OnInit,AfterViewInit,trigger,state,style,transition,animate,keyframes} from '@angular/core';
import {WorkerService} from '../../services/workers.service';
import {Worker} from '../../services/worker.metadata';

@Component({
    moduleId: module.id,
    selector: 'table-cmp',
    templateUrl: 'workers.component.html',
    providers:[WorkerService]
})
export class WorkersComponent implements OnInit{

    public workers : Worker[];
    private errorMessage: string;
    constructor(private workerService :  WorkerService ){
       //this.workers = workerService.getWorkers();
    }

    ngOnInit(){
        this.workerService.getWorkers().subscribe(
            data => this.workers = data,
            error => this.errorMessage = <any>error
        );
        console.log(this.workers);

    }

}
