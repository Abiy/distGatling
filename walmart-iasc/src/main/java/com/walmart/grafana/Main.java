package com.walmart.grafana;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by walmart
 */
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws InterruptedException, IOException {

       // log.info("Some Info message, {}", "test");
            //System.in.read();
        com.walmart.graylog.LiquiMonitor.configure();
        //com.walmart.grafana.LiquiMetrics.configureMetricsDashboard();
        Thread.sleep(Long.parseLong("30000"));

    }
}
