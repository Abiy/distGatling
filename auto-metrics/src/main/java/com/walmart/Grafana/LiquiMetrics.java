package com.walmart.grafana;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.entity.NStringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import rx.apache.http.ObservableHttp;

/**
 * Created by walmart
 */
public class LiquiMetrics {

    private static final Logger log = LoggerFactory.getLogger(LiquiMetrics.class);
    private static final String CONFIG_NAME = "grafana";

    private LiquiMetrics(){
        //Do not initialize this class
    }
    public static void configureMetricsDashboard() {
        //read configuration file, resource or -D
        //Using rxJava create the dashboard async
        //if failure retry and log using slf4j
        Config config = ConfigFactory.load(CONFIG_NAME);

        final RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(3000)
                .setConnectTimeout(500).build();
        final CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setMaxConnPerRoute(2)
                .setMaxConnTotal(5)
                .build();
        httpclient.start();

        final HttpPost request = new HttpPost(getGrafanaUrl(config));
        request.setHeader(HttpHeaders.ACCEPT, String.valueOf(ContentType.APPLICATION_JSON));
        request.setHeader(HttpHeaders.CONTENT_TYPE, String.valueOf(ContentType.APPLICATION_JSON));
        request.setHeader(HttpHeaders.AUTHORIZATION, config.getString("grafana.authorization"));
        try {
            FileFinder finder = new FileFinder();
            File file = new File(finder.getFile("/dashboard.json"));
            String content = FileUtils.readFileToString(file, Charsets.toCharset(StandardCharsets.UTF_8));
            NStringEntity entity = new NStringEntity(content);
            request.setEntity(entity);
            ObservableHttp.createRequest( HttpAsyncMethods.create(request), httpclient)
                    .toObservable()
                    .doOnError(throwable -> {
                        log.error("Error trying to create dashboard {}",throwable);
                    })
                    .map(r -> r.getContent().map(c -> new String(c)))
                    //.toBlocking()
                    .forEach(resp -> log.info("Response for {} ", resp));
        } catch (UnsupportedEncodingException e) {
            log.error("Error trying to create dashboard",e);
        } catch (IOException e) {
            log.error("Error trying to create dashboard", e);
        }
    }


    public static URI getGrafanaUrl(Config config) {
        String url = "http://%s:%s/api/dashboards/db";
        String result = String.format(url,config.getString("grafana.host"),config.getInt("grafana.port"));
        log.info("Grafana link: {}", result);
        return URI.create(result);
    }

}
