import { Component,state,style,animate,transition, trigger,OnDestroy, OnInit} from '@angular/core';
import { FormsModule}   from '@angular/forms';
import {JobSummary, SimulationModel} from '../../services/worker.metadata';
import {WorkerService} from '../../services/workers.service';
import {Http, Response, Headers, Jsonp, RequestOptions} from '@angular/http';
import {Observable} from "rxjs";

import {MultipartItem} from "./multipart-item";
import {MultipartUploader} from "./multipart-uploader";
import { Router } from '@angular/router';

@Component({
    moduleId: module.id,
    selector: 'run-cmp',
    templateUrl: 'run.component.html',
    providers:[WorkerService]
    })

export class RunComponent  implements OnDestroy,OnInit{
    errorMessage: any;
    multipartItem: MultipartItem;
    private uploader:MultipartUploader;
    public model: SimulationModel;
    public  partitions: Array<string>;
    public uploadUrl: string;

    upload : () => void;
    uploadCallback : (data) => void;
    simulationFile: File;
    dataFile: File;
    private success: boolean;


    constructor(private workerService:WorkerService, private _http: Http, private _router: Router){

    }

    ngOnDestroy(): void {
    }


    isValid():boolean {
        return   this.model.partitionName != null && this.model.packageName != null &&
            this.model.parallelism != null;
    }

    navigateToJobDetail(trackingId:string):void{
        this._router.navigate(["/detail/" + trackingId])
    }
    ngOnInit(): void {
        this.success = true;
        this.uploadUrl  = "http://localhost:8080/upload";
        this.uploader = new MultipartUploader({url: this.uploadUrl});
        this.multipartItem =  new MultipartItem(this.uploader);
        this.model = { parallelism:1, accessKey:"", packageName:"", partitionName:"", parameter:"", userName:""};
        this.upload = () => {
            if (null == this.simulationFile || !this.isValid()){
                console.error("run.component.ts & upload() form invalid.");
                return;
            }
            if (this.multipartItem == null){
                this.multipartItem = new MultipartItem(this.uploader);
            }
            if (this.multipartItem.formData == null)
                this.multipartItem.formData = new FormData();

            // this.multipartItem.formData.append("name",  this.model.name);
            this.multipartItem.formData.append("partitionName",  this.model.partitionName);
            this.multipartItem.formData.append("simulationFile",  this.simulationFile);
            this.multipartItem.formData.append("parallelism",  this.model.parallelism);
            this.multipartItem.formData.append("packageName",  this.model.packageName);
            this.multipartItem.formData.append("userName",  this.model.userName);
            this.multipartItem.formData.append("accessKey",  this.model.accessKey);
            this.multipartItem.formData.append("dataFile",  this.dataFile);
            this.multipartItem.formData.append("parameter",  this.model.parameter);

            this.multipartItem.callback = this.uploadCallback;
            this.multipartItem.upload();
        }

        this.uploadCallback = (data) => {
            console.debug("uploadCallback() ==>");
            this.simulationFile = null;
            this.dataFile = null;
            var result = JSON.parse(data)
            if (result.success){
                this.success = true;
                this.navigateToJobDetail(result.trackingId)
                console.log(" uploadCallback() upload file success.");
            }else{
                this.success = false;
                console.log("uploadCallback() upload file false.");
            }
        }
        this.fetchDashboardData();
    }

    selectFile($event): void {
        var inputValue = $event.target;
        if( null == inputValue || null == inputValue.files[0]){
            console.debug("Input file error.");
            return;
        }else {
            this.simulationFile = inputValue.files[0];
            console.debug("Input File name: " + this.simulationFile.name + " type:" + this.simulationFile.size + " size:" + this.simulationFile.size);
        }
    }

   selectDataFile($event): void {
        var inputValue = $event.target;
        if( null == inputValue || null == inputValue.files[0]){
            console.debug("Input file error.");
            return;
        }else {
            this.dataFile = inputValue.files[0];
            console.debug("Input File name: " + this.dataFile.name + " type:" + this.dataFile.size + " size:" + this.dataFile.size);
        }
    }

    private fetchDashboardData():void {
        this.workerService.getDashboardData().subscribe(
            data => {
               this.partitions = data.partition.keys;
               if(this.partitions.length>0){
                    this.model.partitionName = this.partitions[0];
               }
            },
            error => this.errorMessage = <any>error
        );
    }

}
