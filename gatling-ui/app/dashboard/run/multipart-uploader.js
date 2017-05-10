"use strict";
var MultipartUploader = (function () {
    function MultipartUploader(options) {
        this.options = options;
        this.isUploading = false;
        this.progress = 0;
        this.isHTML5 = true;
        this.timeout = 10000;
        // Object.assign(this, options);
        this.url = options.url;
        this.authToken = options.authToken;
    }
    MultipartUploader.prototype.uploadItem = function (item) {
        console.debug("multipart-uploader.ts & uploadItem() ==>.");
        if (this.isUploading) {
            console.debug("multipart-uploader.ts & uploadItem() uploader is uploading now.");
            return;
        }
        this.isUploading = true;
        this._xhrTransport(item);
    };
    MultipartUploader.prototype._onBeforeUploadItem = function (item) {
        item._onBeforeUpload();
    };
    MultipartUploader.prototype._parseHeaders = function (headers) {
        var parsed = {}, key, val, i;
        if (!headers) {
            return parsed;
        }
        headers.split('\n').map(function (line) {
            i = line.indexOf(':');
            key = line.slice(0, i).trim().toLowerCase();
            val = line.slice(i + 1).trim();
            if (key) {
                parsed[key] = parsed[key] ? parsed[key] + ', ' + val : val;
            }
        });
        return parsed;
    };
    MultipartUploader.prototype._transformResponse = function (response, headers) {
        return response;
    };
    MultipartUploader.prototype._isSuccessCode = function (status) {
        return (status >= 200 && status < 300) || status === 304;
    };
    MultipartUploader.prototype._render = function () {
        // todo: ?
    };
    MultipartUploader.prototype._xhrTransport = function (item) {
        var _this = this;
        console.debug("multipart-uploader.ts & _xhrTransport() ==>.");
        var xhr = item._xhr = new XMLHttpRequest();
        xhr.timeout = this.timeout;
        //if (item.formData.length === 0){
        //  throw new TypeError('Invalid form,form is empty.');
        //}
        this._onBeforeUploadItem(item);
        xhr.upload.onprogress = function (event) {
        };
        xhr.onload = function () {
            console.debug("multipart-uploader.ts & _xhrTransport.onload() ==>");
            var headers = _this._parseHeaders(xhr.getAllResponseHeaders());
            var response = _this._transformResponse(xhr.response, headers);
            var gist = _this._isSuccessCode(xhr.status) ? 'Success' : 'Error';
            var method = '_on' + gist + 'Item';
            _this[method](item, response, xhr.status, headers);
            _this._onCompleteItem(item, response, xhr.status, headers);
        };
        xhr.onerror = function () {
            console.debug("multipart-uploader.ts & _xhrTransport.onerror() ==>");
            var headers = _this._parseHeaders(xhr.getAllResponseHeaders());
            var response = _this._transformResponse(xhr.response, headers);
            _this._onErrorItem(item, response, xhr.status, headers);
            //this._onCompleteItem(item, response, xhr.status, headers);
        };
        xhr.ontimeout = function () {
            console.debug("multipart-uploader.ts & _xhrTransport.ontimeout() ==>");
            var headers = _this._parseHeaders(xhr.getAllResponseHeaders());
            var response = _this._transformResponse(xhr.response, headers);
            _this._onErrorItem(item, response, xhr.status, headers);
            //this._onCompleteItem(item, response, xhr.status, headers);
        };
        xhr.onabort = function () {
            console.debug("multipart-uploader.ts & _xhrTransport.onabort() ==>");
            var headers = _this._parseHeaders(xhr.getAllResponseHeaders());
            var response = _this._transformResponse(xhr.response, headers);
            //this._onCancelItem(item, response, xhr.status, headers);
            _this._onCompleteItem(item, response, xhr.status, headers);
        };
        xhr.open(item.method, this.url, true);
        xhr.withCredentials = item.withCredentials;
        if (this.authToken) {
            xhr.setRequestHeader('Authorization', this.authToken);
        }
        console.debug("multipart-uploader.ts & _xhrTransport() send...");
        xhr.send(item.formData);
        this._render();
    };
    MultipartUploader.prototype.onSuccessItem = function (item, response, status, headers) {
    };
    MultipartUploader.prototype.onErrorItem = function (item, response, status, headers) {
        this.isUploading = false;
    };
    MultipartUploader.prototype.onCancelItem = function (item, response, status, headers) {
    };
    MultipartUploader.prototype.onCompleteItem = function (item, response, status, headers) {
    };
    MultipartUploader.prototype._onSuccessItem = function (item, response, status, headers) {
        item._onSuccess(response, status, headers);
        this.onSuccessItem(item, response, status, headers);
    };
    MultipartUploader.prototype._onErrorItem = function (item, response, status, headers) {
        console.debug("multipart-uploader.ts & _onErrorItem() ==>" + " Error status:" + status);
        item._onError(response, status, headers);
        this.onErrorItem(item, response, status, headers);
    };
    MultipartUploader.prototype._onCancelItem = function (item, response, status, headers) {
        item._onCancel(response, status, headers);
        this.onCancelItem(item, response, status, headers);
    };
    MultipartUploader.prototype._onCompleteItem = function (item, response, status, headers) {
        item._onComplete(response, status, headers);
        this.onCompleteItem(item, response, status, headers);
        this.isUploading = false;
        //this.progress = this._getTotalProgress();
        this._render();
    };
    return MultipartUploader;
}());
exports.MultipartUploader = MultipartUploader;
//# sourceMappingURL=multipart-uploader.js.map