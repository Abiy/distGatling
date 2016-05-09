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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
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
        Observable.fromCallable(() -> configureMonitors())
                .map(s -> getGraylogStreamObject(s, mapper))
                .doOnError(e -> log.error("Error parsing graylog content: {}", e))
                .subscribe(graylogStream -> {
                    processStreams(graylogStream,config);
                });
    }

    public String configureMonitors()  {
        final HttpGet request = getHttpGet(config, (host, port) -> getGraylogStreamApiUrl(host,port));
        try (CloseableHttpResponse response = httpClient.execute(request)){
            String jsonString = IOUtils.toString(response.getEntity().getContent(), Charsets.UTF_8);
            log.info("Starting app monitor configuration , existing monitors: {}",jsonString);
            return jsonString;
        } catch (IOException e) {
            log.error("Error configuring app monitors: {}", e);
        }
        return null;
    }

    public String createStream(String jsonStream)  {
        final HttpPost request = getHttpPost(config, (host, port) -> getGraylogStreamApiUrl(host, port));
        try {
            StringEntity entity = new StringEntity(jsonStream);
            request.setEntity(entity);
        } catch (UnsupportedEncodingException e) {
            log.error("Error creating a stream: {}",e);
        }
        try (CloseableHttpResponse response = httpClient.execute(request)){
            String jsonString = IOUtils.toString(response.getEntity().getContent(), Charsets.UTF_8);
            log.info("Response for create stream: {}", jsonString);
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

    /**
     * GET /streams
     * POST /streams
     * @param host
     * @param port
     * @return
     */
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
     * POST /streams/{streamId}/resume
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
        log.info("processStreams: Running process stream... ");
        String targeTitle = config.getString("graylog.stream.title");
        Optional<Stream> targetStream = graylogStream.findStream(targeTitle);
        if(!targetStream.isPresent()){
            try {
                String jsonStream = mapper.writeValueAsString(new Stream(config));
                Observable.fromCallable(() -> createStream(jsonStream))
                        .doOnError(e -> log.error("Error creating graylog stream: {}", e))
                        .map(s -> getStreamIdObject(s, mapper))
                        .doOnNext(streamId -> updateStreamAlertConditions(streamId, config))
                        .doOnNext(streamId -> updateStreamAlertReceivers(streamId, config))
                        .doOnNext(streamId -> updateStreamStatus(streamId, config))
                        .doOnCompleted(() -> log.info("Alert configuration for the specified target application completed."))
                        .subscribe(streamId -> {
                            log.info("Stream id: {} was created and configured. ", streamId.getStream_id());
                        });
            } catch (JsonProcessingException e) {
                log.error("processStreams: Error processing stream content: {}", e);
            }

        }
        else {
            log.info("Alerts for the specified target application is already configured.");
        }
    }

    private  void updateStreamAlertConditions(StreamId streamId, Config config){
        log.info("Running update for stream alert condition: {} ",streamId);
        if(streamId.isValidStream()){
            final HttpPost request = getHttpPost(config, (host, port) -> getAlertConditionsApiUrl(host, port, streamId.getStream_id()));
            try {
                AlertConditions alertConditions = new AlertConditions(config.getConfig("graylog.stream.alert_conditions"));
                //read config and build alert condition
                StringEntity entity = new StringEntity(mapper.writeValueAsString(alertConditions));
                request.setEntity(entity);
            } catch (UnsupportedEncodingException|JsonProcessingException e) {
                log.error("updateStreamAlertConditions: Error updating a stream alert condition: {}",e);
            }
            try (CloseableHttpResponse response = httpClient.execute(request)){
                String jsonString = IOUtils.toString(response.getEntity().getContent(), Charsets.UTF_8);
                log.info("updateStreamAlertConditions result : {}",jsonString);
            } catch (IOException e) {
                log.error("updateStreamAlertConditions: Error updating a stream alert condition: {}",e);
            }
        }
        else {
            log.error("updateStreamAlertConditions: Error Invalid stream id");
        }
    }

    private  void updateStreamAlertReceivers(StreamId streamId, Config config){
        log.info("Running  updating stream alert receivers: {} ",streamId);
        if(streamId.isValidStream()){
            AlertReceiver alertReceiver = new AlertReceiver(config.getConfig("graylog.stream.alert_receivers"),streamId.getStream_id());
            final HttpPost request = getHttpPost(config, (host, port) -> getAlertReceiversApiUrl(host, port, alertReceiver));
            try {
                //read config and build alert condition
                StringEntity entity = new StringEntity(mapper.writeValueAsString(alertReceiver));
                request.setEntity(entity);
            } catch (UnsupportedEncodingException | JsonProcessingException e) {
                log.error("updateStreamAlertReceivers: Error updating stream alert receivers: {}",e);
            }

            try (CloseableHttpResponse response = httpClient.execute(request)){
                String jsonString = IOUtils.toString(response.getEntity().getContent(), Charsets.UTF_8);
                log.info(jsonString);
            } catch (IOException e) {
                log.error("updateStreamAlertReceivers: Error updating stream alert receivers: {}",e);
            }
        }
        else {
            log.error("updateStreamAlertReceivers: Error Invalid stream id");
        }
    }

    private  void updateStreamStatus(StreamId streamId, Config config){
        log.info("Running update updateStreamStatus: {} ");
        if(streamId.isValidStream()){
            final HttpPost request = getHttpPost(config, (host, port) -> getStartStreamApiUrl(host, port, streamId.getStream_id()));
            try (CloseableHttpResponse response = httpClient.execute(request)){
                if(response.getStatusLine().getStatusCode()>=200 && response.getStatusLine().getStatusCode()<300){
                    log.info("updateStreamStatus: stream {} successfully activated.",streamId);
                }
                else {
                    log.error("updateStreamStatus: returned invalid http status code.");
                }
            } catch (IOException e) {
                log.error("updateStreamStatus: Error starting stream a stream: {}", e);
            }
        }
        else {
            log.error("updateStreamStatus: Error Invalid stream id");
        }
    }

}
