package com.walmart.gatling.domain;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by walmart
 */
public class HostUtils {

    public static String lookupHost() {
        InetAddress ip;
        String hostname;
        try {
            ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();
            return hostname;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "UNKNOWN";
    }
}