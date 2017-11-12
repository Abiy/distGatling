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

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import akka.cluster.singleton.ClusterSingletonProxy;
import akka.cluster.singleton.ClusterSingletonProxySettings;
import akka.dispatch.Mapper;
import akka.dispatch.Recover;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.util.Timeout;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import static akka.pattern.Patterns.ask;
import static akka.pattern.Patterns.pipe;

public class MasterClientActor extends AbstractActor {
  private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  private ActorRef masterProxy;

  public  MasterClientActor(ActorSystem system,String masterName){
    ClusterSingletonProxySettings proxySettings =
            ClusterSingletonProxySettings.create(system).withRole(masterName);
    masterProxy = system.actorOf(ClusterSingletonProxy.props("/user/master", proxySettings), "masterProxy" + UUID.randomUUID());
  }

  public void receiveHandler(Object message) {
    log.debug("Master client received: {}",message);
    Timeout timeout = new Timeout(120, TimeUnit.SECONDS);
    Future<Object> future = ask(masterProxy, message, timeout);
    final ExecutionContext ec = getContext().system().dispatcher();

    Future<Object> res = future.map(new Mapper<Object, Object>() {
      @Override
      public Object apply(Object msg) {
          return new Ok(msg);
      }
    }, ec).recover(new Recover<Object>() {
      @Override
      public Object recover(Throwable failure) throws Throwable {
        return new NotOk(null);
      }
    }, ec);

    pipe(res, ec).to(getSender());
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
            .match(Object.class, cmd -> receiveHandler(cmd))
            .build();
  }

  public  static class Ok implements Serializable {
    private Object msg;
    private Ok(Object msg) {
      this.msg=msg;
    }
    public Object getMsg() {
      return msg;
    }

    @Override
    public String toString() {
      return "Ok";
    }
  }


  public static  class NotOk implements Serializable {
    private Object msg;
    private NotOk(Object msg) {
      this.msg=msg;
    }
    public Object getMsg() {
      return msg;
    }

    @Override
    public String toString() {
      return "NotOk";
    }
  }
}
