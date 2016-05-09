package com.walmart.graylog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;

import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * Created by walmart
 */
public class GraylogHttpHandler {

    private static final Logger log = LoggerFactory.getLogger(GraylogHttpHandler.class);
    private final static RequestConfig requestConfig = RequestConfig.custom()
            .setSocketTimeout(3000)
            .setConnectTimeout(500).build();
    private final static CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .setMaxConnPerRoute(20)
            .setMaxConnTotal(5)
            .build();
    private final static  ObjectMapper mapper = new ObjectMapper();


    private GraylogHttpHandler(){
        //Do not initialize this class
    }



    private static HttpGet getHttpGet(Config config,UrlBuilder urlBuilder) {
        final HttpGet request = new HttpGet(urlBuilder.apply(config.getString("graylog.host"), config.getString("graylog.port")));
        request.setHeader(HttpHeaders.ACCEPT, String.valueOf(ContentType.APPLICATION_JSON));
        request.setHeader(HttpHeaders.CONTENT_TYPE, String.valueOf(ContentType.APPLICATION_JSON));
        request.setHeader(HttpHeaders.AUTHORIZATION, config.getString("graylog.authorization"));
        return request;
    }

    private static HttpPost getHttpPost(Config config,UrlBuilder urlBuilder) {
        final HttpPost request = new HttpPost(urlBuilder.apply(config.getString("graylog.host"), config.getString("graylog.port")));
        request.setHeader(HttpHeaders.ACCEPT, String.valueOf(ContentType.APPLICATION_JSON));
        request.setHeader(HttpHeaders.CONTENT_TYPE, String.valueOf(ContentType.APPLICATION_JSON));
        request.setHeader(HttpHeaders.AUTHORIZATION, config.getString("graylog.authorization"));
        return request;
    }

    public static URI getGraylogStreamApiUrl(String host,String port) {
        String url = "http://%s:%s/streams";
        String result = String.format(url,host,port);
        log.info("Graylog Stream link: {}", result);
        return URI.create(result);
    }


}
