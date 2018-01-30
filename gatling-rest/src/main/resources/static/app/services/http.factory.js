"use strict";
var http_interceptor_1 = require("./http.interceptor");
function httpFactory(xhrBackend, requestOptions) {
    return new http_interceptor_1.InterceptedHttp(xhrBackend, requestOptions);
}
exports.httpFactory = httpFactory;
//# sourceMappingURL=http.factory.js.map