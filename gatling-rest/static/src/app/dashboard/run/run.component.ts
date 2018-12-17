import { Component, OnDestroy, OnInit} from '@angular/core';
import { FormsModule}   from '@angular/forms';
import {JobSummary, SimulationModel} from '../../services/worker.metadata';
import {WorkerService} from '../../services/workers.service';
import {Observable} from "rxjs";
import { environment } from '../../../environments/environment';

import {MultipartItem} from "./multipart-item";
import {MultipartUploader} from "./multipart-uploader";
import { Router } from '@angular/router';

@Component({
    selector: 'run-cmp',
    templateUrl: 'run.component.html',
    providers:[WorkerService]
    })

export class RunComponent  implements OnDestroy, OnInit{
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
    resourcesFile: File;
    success: boolean;

    constructor(private workerService:WorkerService, private router: Router){
    }

    ngOnDestroy(): void {
    }


    isValid():boolean {
        return   this.model.partitionName != null && this.model.packageName != null &&
            this.model.parallelism != null;
    }

    navigateToJobDetail(trackingId:string):void{
        this.router.navigate(["/detail/" + trackingId])
    }

    ngOnInit(): void {
        this.success = true;
        this.uploadUrl  = environment.apiUrl + "/upload";
        
        this.uploader = new MultipartUploader({url: this.uploadUrl, authToken: localStorage.getItem('currentUser')});
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
            this.multipartItem.formData.append("parallelism",  this.model.parallelism.toString());
            this.multipartItem.formData.append("packageName",  this.model.packageName);
            this.multipartItem.formData.append("userName",  this.model.userName);
            this.multipartItem.formData.append("accessKey",  this.model.accessKey);
            this.multipartItem.formData.append("resourcesFile",  this.resourcesFile);
            this.multipartItem.formData.append("parameter",  this.model.parameter);

            this.multipartItem.callback = this.uploadCallback;
            this.multipartItem.upload();
        };

        this.uploadCallback = (data) => {
            console.debug("uploadCallback() ==>");
            this.simulationFile = null;
            this.dataFile = null;
            this.resourcesFile = null;
            var result = JSON.parse(data);
            if (result.success){
                this.success = true;
                this.navigateToJobDetail(result.trackingId);
                console.log("uploadCallback() upload file success.");
            }else{
                this.success = false;
                console.log("uploadCallback() upload file false.");
            }
        };
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

    selectResourcesFile($event): void {
        var inputValue = $event.target;
        if( null == inputValue || null == inputValue.files[0]){
            console.debug("Input file error.");
            return;
        }else {
            this.resourcesFile = inputValue.files[0];
            console.debug("Input File name: " + this.resourcesFile.name + " type:" + this.resourcesFile.size + " size:" + this.resourcesFile.size);
        }
    }

    private fetchDashboardData():void {
        this.workerService.getDashboardData().subscribe(
            data => {
               this.partitions = data.partition.keys;
               if(this.partitions.length > 0){
                    this.model.partitionName = this.partitions[0];
               }
            },
            error => this.errorMessage = <any>error
        );
    }

}
