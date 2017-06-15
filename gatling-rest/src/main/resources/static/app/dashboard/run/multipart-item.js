"use strict";
var MultipartItem = (function () {
    function MultipartItem(uploader) {
        this.uploader = uploader;
        this.alias = 'file';
        this.url = '/';
        this.method = 'POST';
        this.headers = [];
        this.withCredentials = true;
        this.formData = null;
        this.isReady = false;
        this.isUploading = false;
        this.isUploaded = false;
        this.isSuccess = false;
        this.isCancel = false;
        this.isError = false;
        this.progress = 0;
        this.index = null;
        this.callback = null;
    }
    MultipartItem.prototype.upload = function () {
        try {
            console.debug("multipart-item.ts & upload() ==>.");
            this.uploader.uploadItem(this);
        }
        catch (e) {
        }
    };
    MultipartItem.prototype.init = function () {
        this.isReady = false;
        this.isUploading = false;
        this.isUploaded = false;
        this.isSuccess = false;
        this.isCancel = false;
        this.isError = false;
        this.progress = 0;
        this.formData = null;
        this.callback = null;
    };
    MultipartItem.prototype.onBeforeUpload = function () {
    };
    MultipartItem.prototype.onProgress = function (progress) {
    };
    MultipartItem.prototype.onSuccess = function (response, status, headers) {
    };
    MultipartItem.prototype.onError = function (response, status, headers) {
    };
    MultipartItem.prototype.onCancel = function (response, status, headers) {
    };
    MultipartItem.prototype.onComplete = function (response, status, headers) {
        this.callback(response);
        this.init();
    };
    MultipartItem.prototype._onBeforeUpload = function () {
        this.isReady = true;
        this.isUploading = true;
        this.isUploaded = false;
        this.isSuccess = false;
        this.isCancel = false;
        this.isError = false;
        this.progress = 0;
        this.onBeforeUpload();
    };
    MultipartItem.prototype._onProgress = function (progress) {
        this.progress = progress;
        this.onProgress(progress);
    };
    MultipartItem.prototype._onSuccess = function (response, status, headers) {
        this.isReady = false;
        this.isUploading = false;
        this.isUploaded = true;
        this.isSuccess = true;
        this.isCancel = false;
        this.isError = false;
        this.progress = 100;
        this.index = null;
        this.onSuccess(response, status, headers);
    };
    MultipartItem.prototype._onError = function (response, status, headers) {
        this.isReady = false;
        this.isUploading = false;
        this.isUploaded = true;
        this.isSuccess = false;
        this.isCancel = false;
        this.isError = true;
        this.progress = 0;
        this.index = null;
        this.onError(response, status, headers);
        this.callback(response);
    };
    MultipartItem.prototype._onCancel = function (response, status, headers) {
        this.isReady = false;
        this.isUploading = false;
        this.isUploaded = false;
        this.isSuccess = false;
        this.isCancel = true;
        this.isError = false;
        this.progress = 0;
        this.index = null;
        this.onCancel(response, status, headers);
    };
    MultipartItem.prototype._onComplete = function (response, status, headers) {
        this.onComplete(response, status, headers);
    };
    MultipartItem.prototype._prepareToUploading = function () {
        this.isReady = true;
    };
    return MultipartItem;
}());
exports.MultipartItem = MultipartItem;
//# sourceMappingURL=multipart-item.js.map