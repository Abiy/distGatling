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
import akka.util.Timeout;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import static akka.pattern.Patterns.ask;
import static akka.pattern.Patterns.pipe;

public class MasterClientActor extends UntypedActor {

  ActorRef masterProxy;

  public  MasterClientActor(ActorSystem system){

    ClusterSingletonProxySettings proxySettings =
            ClusterSingletonProxySettings.create(system).withRole("backend");
    masterProxy =system.actorOf(ClusterSingletonProxy.props("/user/master", proxySettings), "masterProxy" + UUID.randomUUID());
  }

  public void onReceive(Object message) {

    Timeout timeout = new Timeout(5, TimeUnit.SECONDS);
    Future<Object> f = ask(masterProxy, message, timeout);

    final ExecutionContext ec = getContext().system().dispatcher();

    Future<Object> res = f.map(new Mapper<Object, Object>() {
      @Override
      public Object apply(Object msg) {
        if (msg instanceof Master.ServerInfo)
          return new Ok(msg);
        else
          return new NotOk(msg);
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
