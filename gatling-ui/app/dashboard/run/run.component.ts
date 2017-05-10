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
    multipartItem:MultipartItem ;
    private uploader:MultipartUploader;
    public model: SimulationModel;
    public  partitions: Array<string>;
    public uploadUrl: string;

    upload : () => void;
    uploadCallback : (data) => void;
    file: File;
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
        this.model = { parallelism:1, accessKey:"", packageName:"", partitionName:"", tag:"", userName:""};
        this.partitions = ["aire","soar","ei"]
        this.upload = () => {
            if (null == this.file || !this.isValid()){
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
            this.multipartItem.formData.append("file",  this.file);
            this.multipartItem.formData.append("parallelism",  this.model.parallelism);
            this.multipartItem.formData.append("packageName",  this.model.packageName);
            this.multipartItem.formData.append("userName",  this.model.userName);
            this.multipartItem.formData.append("accessKey",  this.model.accessKey);

            this.multipartItem.callback = this.uploadCallback;
            this.multipartItem.upload();
        }

        this.uploadCallback = (data) => {
            console.debug("uploadCallback() ==>");
            this.file = null;
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


    }

    selectFile($event): void {
        var inputValue = $event.target;
        if( null == inputValue || null == inputValue.files[0]){
            console.debug("Input file error.");
            return;
        }else {
            this.file = inputValue.files[0];
            console.debug("Input File name: " + this.file.name + " type:" + this.file.size + " size:" + this.file.size);
        }
    }

  /*  submitSimulation(): boolean{
        return true;
    }

   fileChange() {

        let fileList = this.fileInput.nativeElement.files;
       // let fileList: FileList = event.target.files;
        if(fileList.length > 0) {
            let file: File = fileList[0];
            let formData:FormData = new FormData();

            formData.append('fileName', file, file.name);
            //formData.append("partition",this.model.partitionName);
            //formData.append("name",this.model.name);*!/
            let headers = new Headers();
            headers.append('Content-Type', 'multipart/form-data');
            headers.append('Accept', 'application/json');
            let options = new RequestOptions({ headers: headers });
            this._http.post(this.uploadUrl, formData, options)
                .map(res => res.json())
                .catch(error => Observable.throw(error))
                .subscribe(
                    data => console.log('success'),
                    error => console.log(error)
                )
        }
    }*/

}
