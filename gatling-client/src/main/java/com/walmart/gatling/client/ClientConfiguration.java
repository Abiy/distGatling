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

import akka.actor.ActorSystem;
import com.walmart.gatling.commons.ClientConfig;
import com.walmart.gatling.commons.HostUtils;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.Base64Utils;

/**  */
@Configuration
public class ClientConfiguration {
  protected final Log logger = LogFactory.getLog(getClass());

  @Value("${client.numberOfActors}")
  private int numberOfActors;

  @Value("${client.port}")
  private int port;

  @Value("${client.role}")
  private String role;

  @Value("${server.port}")
  private int clientPort;

  @Value("${akka.contact-points}")
  private String contactPoints;

  @Value("${server.url}")
  private String serverUrl;

  @Bean
  public ClientConfig configBuilder(Environment env) {
    ClientConfig clientConfig = new ClientConfig();
    clientConfig.setNumberOfActors(1);
    clientConfig.setPort(port);
    clientConfig.setRole(role);
    clientConfig.setContactPoint(contactPoints);
    clientConfig.setAccessKey(env.getProperty("client.accessKey"));
    clientConfig.setPartitionName(env.getProperty("client.partitionName"));
    clientConfig.setClassName(env.getProperty("client.className"));
    clientConfig.setParameterString(env.getProperty("client.parameter"));
    clientConfig.setResourcesFeedPath(env.getProperty("client.resourcesFeedPath"));
    clientConfig.setResourcesFeedFileName(env.getProperty("client.resourcesFeedFileName"));
    clientConfig.setQuiet(Boolean.parseBoolean(env.getProperty("client.quiet")));
    clientConfig.setParallelism(Short.parseShort(env.getProperty("client.parallelism")));
    clientConfig.setJarPath(env.getProperty("client.jarPath"));
    clientConfig.setJarFileName(env.getProperty("client.jarFileName"));
    clientConfig.setUserName(env.getProperty("client.userName"));
    clientConfig.setHost(HostUtils.lookupHost());
    clientConfig.setRemoteArtifact(Boolean.parseBoolean(env.getProperty("client.remoteArtifact")));

    return clientConfig;
  }

  /**
   * Bean factory that creates the actor system with workers and joins the cluster/master using the
   * contact-point property value
   *
   * @param clientConfig
   * @return
   */
  @Bean
  public ActorSystem createActorSystemWithAgent(ClientConfig clientConfig, Environment env) {

    if (!clientConfig.isRemoteArtifact()) {
      String usPwd = env.getProperty("gatling.user") + ":" + env.getProperty("gatling.password");
      String basicToken = "Basic " + Base64Utils.encodeToString(usPwd.getBytes());
      // upload files here
      String jarFileFullPath;
      boolean uploadSucceeded = true;
      try {
        jarFileFullPath = UploadUtils.uploadFile(serverUrl, clientConfig.getJarPath(), basicToken);
        clientConfig.setJarPath(jarFileFullPath);
        if (!clientConfig.getResourcesFeedPath().isEmpty()) {
          String resourcesFileFullPath =
              UploadUtils.uploadFile(serverUrl, clientConfig.getResourcesFeedPath(), basicToken);
          clientConfig.setResourcesFeedPath(resourcesFileFullPath);
        }
      } catch (IOException| RuntimeException e) {
        uploadSucceeded = false;
        logger.error("Upload failed.",e);
      }
      if (uploadSucceeded) {
        return ClientFactory.startCommandClient(clientConfig);
      }
      return null;
    } else {
      return ClientFactory.startCommandClient(clientConfig);
    }
  }
}
