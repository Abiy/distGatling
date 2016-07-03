package com.walmart.gatling.commons;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

public class MasterClientActor extends UntypedActor {
  private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  private ActorRef masterProxy;

  public  MasterClientActor(ActorSystem system,String masterName){
    ClusterSingletonProxySettings proxySettings =
            ClusterSingletonProxySettings.create(system).withRole(masterName);
    masterProxy = system.actorOf(ClusterSingletonProxy.props("/user/master", proxySettings), "masterProxy" + UUID.randomUUID());
  }

  public void onReceive(Object message) {
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
