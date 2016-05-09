package com.walmart.grafana;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by walmart
 */
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws InterruptedException {

        com.walmart.graylog.LiquiMonitor.configure();
        com.walmart.grafana.LiquiMetrics.configure();
        Thread.sleep(Long.parseLong("30000"));

    }
}
