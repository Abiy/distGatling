package com.walmart.graylog;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by walmart
 */
public class LiquiMonitor {

    private static final Logger log = LoggerFactory.getLogger(LiquiMonitor.class);
    private static final String CONFIG_NAME = "monitor";


    /**
     *  Create an HttpClient with the ThreadSafeClientConnManager.
     *  This connection manager must be used if more than one thread will be using the HttpClient.
     * @throws IOException
     */
    public static void configure() throws IOException {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);
        final RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(3000)
                .setConnectTimeout(500).build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .build();
        final Config config = ConfigFactory.load(CONFIG_NAME);

        new MonitorService(config,httpClient).configureAppAlerts();
    }

    public String getFile(String fileName){
        return  this.getClass().getResource(fileName).getFile();
    }

}
