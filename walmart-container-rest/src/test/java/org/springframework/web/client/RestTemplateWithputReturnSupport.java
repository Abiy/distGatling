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