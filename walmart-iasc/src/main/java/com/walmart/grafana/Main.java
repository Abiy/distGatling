package com.walmart.grafana;

/**
 * Created by walmart
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {

        com.walmart.graylog.LiquiMonitor.configure();
        com.walmart.grafana.LiquiMetrics.configure();
        Thread.sleep(Long.parseLong("30000"));

    }
}
