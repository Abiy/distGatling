package com.walmart.graylog;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.walmart.grafana.LiquiMetrics;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by walmart
 */
public class LiquiMonitor {

    private static final Logger log = LoggerFactory.getLogger(LiquiMonitor.class);
    private static final String CONFIG_NAME = "monitor";


    /**
     * Create an HttpClient with the ThreadSafeClientConnManager. This connection manager must be
     * used if more than one thread will be using the HttpClient.
     */
    public static void configure() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(5);
        final RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(8000)
                .setConnectTimeout(5000).build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .build();
        log.info("Loading configuration from: {}.conf", CONFIG_NAME);
        final Config config = ConfigFactory.load(CONFIG_NAME);

        new MonitorService(config, httpClient).configureAppAlerts();
        cm.close();
    }

    public static void configure(boolean enableMonitors, boolean createDashboard) {

        Observable.fromCallable(() -> {
            if (enableMonitors)
                LiquiMonitor.configure();
            if (createDashboard)
                LiquiMetrics.configure();
            return null;
        }).subscribeOn(Schedulers.newThread()).subscribe();

    }

}
