/*
 *
 *   Copyright 2016 alh Technology
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

package com.alh.gatling.commons;

import java.net.*;
import java.util.Enumeration;

/**
 *
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
        String ipAddress;
        ipAddress = System.getProperty("bind.ip.address",getLocalAddress());
        return ipAddress;
    }

    private static String getLocalAddress(){
        try {
            Enumeration<NetworkInterface> b = NetworkInterface.getNetworkInterfaces();
            while( b.hasMoreElements()){
                for ( InterfaceAddress f : b.nextElement().getInterfaceAddresses())
                    if ( f.getAddress().isSiteLocalAddress())
                        return f.getAddress().getHostAddress();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "UNKNOWN";
    }
}