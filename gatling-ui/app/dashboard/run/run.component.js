"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var core_1 = require('@angular/core');
var workers_service_1 = require('../../services/workers.service');
var http_1 = require('@angular/http');
var multipart_item_1 = require("./multipart-item");
var multipart_uploader_1 = require("./multipart-uploader");
var router_1 = require('@angular/router');
var RunComponent = (function () {
    function RunComponent(workerService, _http, _router) {
        this.workerService = workerService;
        this._http = _http;
        this._router = _router;
    }
    RunComponent.prototype.ngOnDestroy = function () {
    };
    RunComponent.prototype.isValid = function () {
        return this.model.partitionName != null && this.model.packageName != null &&
            this.model.parallelism != null;
    };
    RunComponent.prototype.navigateToJobDetail = function (trackingId) {
        this._router.navigate(["/detail/" + trackingId]);
    };
    RunComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.success = true;
        this.uploadUrl = "http://localhost:8080/upload";
        this.uploader = new multipart_uploader_1.MultipartUploader({ url: this.uploadUrl });
        this.multipartItem = new multipart_item_1.MultipartItem(this.uploader);
        this.model = { parallelism: 1, accessKey: "", packageName: "", partitionName: "", tag: "", userName: "" };
        this.partitions = ["aire", "soar", "ei"];
        this.upload = function () {
            if (null == _this.file || !_this.isValid()) {
                console.error("run.component.ts & upload() form invalid.");
                return;
            }
            if (_this.multipartItem == null) {
                _this.multipartItem = new multipart_item_1.MultipartItem(_this.uploader);
            }
            if (_this.multipartItem.formData == null)
                _this.multipartItem.formData = new FormData();
            // this.multipartItem.formData.append("name",  this.model.name);
            _this.multipartItem.formData.append("partitionName", _this.model.partitionName);
            _this.multipartItem.formData.append("file", _this.file);
            _this.multipartItem.formData.append("parallelism", _this.model.parallelism);
            _this.multipartItem.formData.append("packageName", _this.model.packageName);
            _this.multipartItem.formData.append("userName", _this.model.userName);
            _this.multipartItem.formData.append("accessKey", _this.model.accessKey);
            _this.multipartItem.callback = _this.uploadCallback;
            _this.multipartItem.upload();
        };
        this.uploadCallback = function (data) {
            console.debug("uploadCallback() ==>");
            _this.file = null;
            var result = JSON.parse(data);
            if (result.success) {
                _this.success = true;
                _this.navigateToJobDetail(result.trackingId);
                console.log(" uploadCallback() upload file success.");
            }
            else {
                _this.success = false;
                console.log("uploadCallback() upload file false.");
            }
        };
    };
    RunComponent.prototype.selectFile = function ($event) {
        var inputValue = $event.target;
        if (null == inputValue || null == inputValue.files[0]) {
            console.debug("Input file error.");
            return;
        }
        else {
            this.file = inputValue.files[0];
            console.debug("Input File name: " + this.file.name + " type:" + this.file.size + " size:" + this.file.size);
        }
    };
    RunComponent = __decorate([
        core_1.Component({
            moduleId: module.id,
            selector: 'run-cmp',
            templateUrl: 'run.component.html',
            providers: [workers_service_1.WorkerService]
        }), 
        __metadata('design:paramtypes', [workers_service_1.WorkerService, http_1.Http, router_1.Router])
    ], RunComponent);
    return RunComponent;
}());
exports.RunComponent = RunComponent;
//# sourceMappingURL=run.component.js.map