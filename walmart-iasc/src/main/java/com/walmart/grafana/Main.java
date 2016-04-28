package com.walmart.grafana;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by walmart
 */
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws InterruptedException {

        log.info("Some Info message, {}", "test");
            //System.in.read();
        LiquiMetrics.configureMetricsDashboard();
        Thread.sleep(Long.parseLong("30000"));

    }
}
