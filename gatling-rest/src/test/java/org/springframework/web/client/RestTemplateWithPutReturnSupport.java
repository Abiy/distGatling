/*
 *
 *   Copyright 2016 Walmart Technology
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.springframework.web.client;


import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * Created by wal-mart
 */
public class RestTemplateWithPutReturnSupport extends TestRestTemplate {

    public <T> T putForObject(String url, Object request, Class<T> responseType,
                              Object... uriVariables) throws RestClientException {
        RequestCallback requestCallback = httpEntityCallback(request, responseType);
        HttpMessageConverterExtractor<T> responseExtractor =
                new HttpMessageConverterExtractor<T>(responseType, getMessageConverters(), logger);
        return execute(url, HttpMethod.PUT, requestCallback, responseExtractor, uriVariables);
    }

    public <T> ResponseEntity<T> putForEntity(String url, Object request, Class<T> responseType,
                                              Object... uriVariables) {
        RequestCallback requestCallback = httpEntityCallback(request, responseType);
        ResponseExtractor<ResponseEntity<T>> responseExtractor =
                responseEntityExtractor(responseType);
        return execute(url, HttpMethod.PUT, requestCallback, responseExtractor, uriVariables);
    }
}