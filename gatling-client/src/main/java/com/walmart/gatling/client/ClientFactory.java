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

package com.walmart.gatling.client;

import akka.actor.ActorPath;
import akka.actor.ActorPaths;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.client.ClusterClient;
import akka.cluster.client.ClusterClientSettings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.walmart.gatling.commons.ClientConfig;
import com.walmart.gatling.commons.CommandClientActor;
import com.walmart.gatling.commons.Constants;
import com.walmart.gatling.commons.HostUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ClientFactory {

    public static ActorSystem startCommandClient(ClientConfig clientConfig) {
        Config conf = ConfigFactory.parseString("akka.cluster.roles=[" + clientConfig.getRole() + "]")
                .withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=" + clientConfig.getPort()))
                .withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname=" + HostUtils.lookupIp()))
                .withFallback(ConfigFactory.load("application"));

        ActorSystem system = ActorSystem.create(Constants.PerformanceSystem, conf);

        Set<ActorPath> initialContacts = new HashSet<>(clientConfig.getContactPoint()
                    .map(p->ActorPaths.fromString(p))
                    .collect(Collectors.toList()));

        ClusterClientSettings settings =  ClusterClientSettings.create(system).withInitialContacts(initialContacts);
        final ActorRef clusterClient = system.actorOf(ClusterClient.props(settings), "clusterClient");

        system.actorOf(CommandClientActor.props(clusterClient, clientConfig), clientConfig.getRole() );

        return system;

    }


}
