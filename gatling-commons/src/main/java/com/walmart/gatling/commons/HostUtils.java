/*
 *
 *   Copyright 2016 Walmart Technology
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

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