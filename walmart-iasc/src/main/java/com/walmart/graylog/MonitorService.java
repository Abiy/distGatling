package com.walmart.graylog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.nio.entity.NStringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Optional;

import rx.Observable;

/**
 * Created by walmart
 */
public class MonitorService {

    private static final Logger log = LoggerFactory.getLogger(MonitorService.class);
    private Config config;
    private CloseableHttpClient httpClient;
    private final  ObjectMapper mapper;

    public MonitorService(Config config,CloseableHttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    }

    public void configureAppAlerts()  {
        Observable.fromCallable(() -> doAlertConfiguration())
                .map(s -> getGraylogStreamObject(s, mapper))
                .doOnError(e -> log.error("Error parsing graylog content: {}", e))
                .subscribe(graylogStream -> {
                    processStreams(graylogStream,config);
                });
    }

    public String doAlertConfiguration()  {
        final HttpGet request = getHttpGet(config, (host, port) -> getGraylogStreamApiUrl(host,port));
        //CloseableHttpResponse response = null;
        try (CloseableHttpResponse response = httpClient.execute(request)){
            String jsonString = IOUtils.toString(response.getEntity().getContent(), Charsets.UTF_8);
            log.info(jsonString);
            return jsonString;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void doCreateStream(String jsonStream){
        Observable.fromCallable(() -> createStream(jsonStream))
                .doOnError(e -> log.error("Error parsing graylog content: {}", e))
                .map(s -> getStreamIdObject(s, mapper))
                .doOnNext(streamId -> updateStreamAlertConditions(streamId, config))
                .doOnNext(streamId -> updateStreamAlertReceivers(streamId, config))
                .doOnNext(streamId -> updateStreamStatus(streamId, config))
                .subscribe(streamId -> {
                    log.info("Stream id: {} was created and configured. ", streamId.getStream_id());
                });
    }

    public String createStream(String jsonStream)  {
        final HttpPost request = getHttpPost(config, (host, port) -> getGraylogStreamApiUrl(host, port));
        try {
            NStringEntity entity = new NStringEntity(jsonStream);
            request.setEntity(entity);
        } catch (UnsupportedEncodingException e) {
            log.error("Error creating a stream: {}",e);
        }
        try (CloseableHttpResponse response = httpClient.execute(request)){
            String jsonString = IOUtils.toString(response.getEntity().getContent(), Charsets.UTF_8);
            log.info(jsonString);
            return jsonString;
        } catch (IOException e) {
            log.error("Error creating a stream: {}", e);
        }
        return null;
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

    private static GraylogStream getGraylogStreamObject(String content, ObjectMapper mapper) {
        try {
            return mapper.readValue(content,GraylogStream.class);
        } catch (IOException e) {
            log.error("Error parsing graylog content: {}", e);
            return new GraylogStream();
        }
    }

    private static StreamId getStreamIdObject(String content, ObjectMapper mapper) {
        try {
            return mapper.readValue(content,StreamId.class);
        } catch (IOException e) {
            log.error("Error parsing graylog content: {}", e);
            return new StreamId();
        }
    }

    public static URI getGraylogStreamApiUrl(String host,String port) {
        String url = "http://%s:%s/streams";
        String result = String.format(url,host,port);
        log.info("Graylog Stream link: {}", result);
        return URI.create(result);
    }
    /**
     * POST /streams/{streamId}/alerts/conditions
     */
    public static URI getAlertConditionsApiUrl(String host,String port,String streamId) {
        String url = "http://%s:%s/streams/%s/alerts/conditions";
        String result = String.format(url,host,port,streamId);
        log.info("Graylog Stream alert condition link: {}", result);
        return URI.create(result);
    }

    /**
     * POST /streams/{streamId}/alerts/receivers
     */
    public static URI getAlertReceiversApiUrl(String host,String port,AlertReceiver streamId) {
        String url = "http://%s:%s/streams/%s/alerts/receivers?entity=%s&type=%s";
        String result = String.format(url,host,port,streamId.getStreamId(),streamId.getEntity(),streamId.getType());
        log.info("Graylog Stream alert receivers link: {}", result);
        return URI.create(result);
    }


    /**
     * POST /streams/{streamId}/alerts/receivers
     */
    public static URI getStartStreamApiUrl(String host,String port,String streamId) {
        String url = "http://%s:%s/streams/%s/resume";
        String result = String.format(url,host,port,streamId);
        log.info("Graylog Stream status link: {}", result);
        return URI.create(result);
    }
    /**
     * //create stream and update alert conditions, update alert receivers, update alert web hook, activate stream
     * @param graylogStream
     * @param config
     */
    private  void processStreams(GraylogStream graylogStream,Config config){
        log.info("processStreams: Running process stream: {} ");
        String targeTitle = config.getString("graylog.stream.title");
        Optional<Stream> targetStream = graylogStream.findStream(targeTitle);
        if(!targetStream.isPresent()){
            try {
                String content = mapper.writeValueAsString(new Stream(config));
                doCreateStream(content);
                log.info(content);
            } catch (JsonProcessingException e) {
                log.error("processStreams: Error processing stream content: {}", e);
            }

        }
        else {
            //update the stream
        }
    }

    private  void updateStreamAlertConditions(StreamId streamId, Config config){
        log.info("Running update stream: {} ");
        if(streamId.isValidStream()){
            final HttpPost request = getHttpPost(config, (host, port) -> getAlertConditionsApiUrl(host, port, streamId.getStream_id()));
            try {
                AlertConditions alertConditions = new AlertConditions(config.getConfig("graylog.stream.alert_conditions"));
                //read config and build alert condition
                NStringEntity entity = new NStringEntity(mapper.writeValueAsString(alertConditions));
                request.setEntity(entity);
            } catch (UnsupportedEncodingException e) {
                log.error("updateStreamAlertConditions: Error creating a stream: {}",e);
            } catch (JsonProcessingException e) {
                log.error(" updateStreamAlertConditions: Error creating a stream: {}", e);
            }
            try (CloseableHttpResponse response = httpClient.execute(request)){
                String jsonString = IOUtils.toString(response.getEntity().getContent(), Charsets.UTF_8);
                log.info("updateStreamAlertConditions result : {}",jsonString);
            } catch (IOException e) {
                log.error("updateStreamAlertConditions: Error creating a stream: {}", e);
            }
        }
    }

    private  void updateStreamAlertReceivers(StreamId streamId, Config config){
        log.info("Running  updateStreamAlertReceivers: {} ");
        if(streamId.isValidStream()){
            AlertReceiver alertReceiver = new AlertReceiver(config.getConfig("graylog.stream.alert_receivers"),streamId.getStream_id());
            final HttpPost request = getHttpPost(config, (host, port) -> getAlertReceiversApiUrl(host, port, alertReceiver));
            try {
                //read config and build alert condition
                NStringEntity entity = new NStringEntity(mapper.writeValueAsString(alertReceiver));
                request.setEntity(entity);
            } catch (UnsupportedEncodingException e) {
                log.error("updateStreamAlertReceivers: Error creating a stream: {}",e);
            } catch (JsonProcessingException e) {
                log.error(" updateStreamAlertReceivers: Error creating a stream: {}", e);
            }
            try (CloseableHttpResponse response = httpClient.execute(request)){
                String jsonString = IOUtils.toString(response.getEntity().getContent(), Charsets.UTF_8);
                log.info(jsonString);
            } catch (IOException e) {
                log.error("updateStreamAlertReceivers: Error creating a stream: {}", e);
            }
        }
    }

    private  void updateStreamStatus(StreamId streamId, Config config){
        log.info("Running update updateStreamStatus: {} ");
        if(streamId.isValidStream()){
            final HttpPost request = getHttpPost(config, (host, port) -> getStartStreamApiUrl(host, port, streamId.getStream_id()));
            try (CloseableHttpResponse response = httpClient.execute(request)){
                String jsonString = IOUtils.toString(response.getEntity().getContent(), Charsets.UTF_8);
                log.info(jsonString);
            } catch (IOException e) {
                log.error("updateStreamStatus: Error starting stream a stream: {}", e);
            }
        }
    }

}
