package com.walmart.grafana;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Created by walmart
 */
public class LiquiMetrics {

    private static final Logger log = LoggerFactory.getLogger(com.walmart.grafana.LiquiMetrics.class);
    private static final String CONFIG_NAME = "monitor";

    private LiquiMetrics(){
        //Do not initialize this class
    }


    public static void configure() {
        log.info("Loading configuration from: {}.conf",CONFIG_NAME);
        final Config config = ConfigFactory.load(CONFIG_NAME);

        final HttpPost request = new HttpPost(getGrafanaUrl(config));
        request.setHeader(HttpHeaders.ACCEPT, String.valueOf(ContentType.APPLICATION_JSON));
        request.setHeader(HttpHeaders.CONTENT_TYPE, String.valueOf(ContentType.APPLICATION_JSON));
        request.setHeader(HttpHeaders.AUTHORIZATION, config.getString("grafana.authorization"));


        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(1);
        final RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(3000)
                .setConnectTimeout(500).build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .build();
        try {
            request.setEntity(getnStringEntity());
        } catch (IOException e) {
            log.error("Error trying to create dashboard {}",e);
        }

        try (CloseableHttpResponse response = httpClient.execute(request)){
            String jsonString = IOUtils.toString(response.getEntity().getContent(), Charsets.UTF_8);
            log.info("Response from grafana: {}",jsonString);
        } catch (IOException e) {
            log.error("Error trying to create dashboard {}",e);
        }
        finally {
            cm.close();
        }
    }

    private static StringEntity getnStringEntity() throws IOException {
        FileFinder finder = new FileFinder();
        File file = new File(finder.getFile("/dashboard.json"));
        String content = FileUtils.readFileToString(file, Charsets.toCharset(StandardCharsets.UTF_8));
        return new StringEntity(content);
    }


    public static URI getGrafanaUrl(Config config) {
        String url = "http://%s:%s/api/dashboards/db";
        String result = String.format(url,config.getString("grafana.host"),config.getInt("grafana.port"));
        log.info("Grafana link: {}", result);
        return URI.create(result);
    }

}
