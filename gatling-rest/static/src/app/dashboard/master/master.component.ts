import {Component, OnInit} from '@angular/core';
import {WorkerService} from '../../services/workers.service';
import { Router } from '@angular/router';
import { KeyVal } from '../../services/worker.metadata';

@Component({
    //moduleId: module.id,
    selector: 'home-cmp',
    templateUrl: 'master.component.html',
    providers: [WorkerService]
})

export class MasterComponent implements OnInit{
    public metrics: Array<KeyVal>;
    private errorMessage: string;

    constructor(private workerService: WorkerService, private router: Router) { }

    ngOnInit(): void {
        this.fetchData();
    }

    fetchData(): void {
        this.workerService.getMasterMetrics().subscribe(
            data => this.metrics = this.transform(data),
            error => this.errorMessage = <any>error
        );
    }

    transform(dict: Object): Array<KeyVal> {
        var a = [];
        for (var key in dict) {
            if (dict.hasOwnProperty(key)) {
                a.push({key: key, val: dict[key]});
            }
        }
        return a;
    }

    getMemoryMetrics(input:Array<KeyVal>): Array<KeyVal>{
       let result: Array<KeyVal> = [];
       for( let entry of input) {
           if (entry.key.startsWith("memory.heap."))
           result.push(entry)
       }
       return result;
    }

    getThreadMetrics(input:Array<KeyVal>): Array<KeyVal>{
        let result: Array<KeyVal> = [];
        for( let entry of input) {
            if (entry.key.startsWith("threads.") )
                result.push(entry)
        }
        return result;
    }

    getGcMetrics(input:Array<KeyVal>): Array<KeyVal>{
        let result: Array<KeyVal> = [];
        for( let entry of input) {
            if (entry.key.startsWith("gc.") )
                result.push(entry)
        }
        return result;
    }
}
