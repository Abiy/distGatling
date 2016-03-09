package com.walmart.gatling.commons;

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

    public static String lookupIp() {
        InetAddress ip;
        String ipAddress;
        try {
            ipAddress = InetAddress.getLocalHost().getHostAddress();
            return ipAddress;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "UNKNOWN";
    }
}