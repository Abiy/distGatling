package com.walmart.gatling.commons;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
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

  final  ActorRef masterProxy;

  public  MasterClientActor(){

    ClusterSingletonProxySettings proxySettings =
            ClusterSingletonProxySettings.create(getContext().system()).withRole("backend");
    masterProxy = getContext().actorOf(
            ClusterSingletonProxy.props("/user/master/active", proxySettings)
            , "masterProxy");
  }

  public void onReceive(Object message) {

    Timeout timeout = new Timeout(5, TimeUnit.SECONDS);
    Future<Object> f = ask(masterProxy, message, timeout);


    final ExecutionContext ec = getContext().system().dispatcher();

    Future<Object> res = f.map(new Mapper<Object, Object>() {
      @Override
      public Object apply(Object msg) {
        if (msg instanceof Master.Ack)
          return Ok.getInstance();
        else
          return super.apply(msg);
      }
    }, ec).recover(new Recover<Object>() {
      @Override
      public Object recover(Throwable failure) throws Throwable {
        return NotOk.getInstance();
      }
    }, ec);

    pipe(res, ec).to(getSender());
  }

  public static final class Ok implements Serializable {
    private Ok() {}

    private static final Ok instance = new Ok();

    public static Ok getInstance() {
      return instance;
    }

    @Override
    public String toString() {
      return "Ok";
    }
  };

  public static final class NotOk implements Serializable {
    private NotOk() {}

    private static final NotOk instance = new NotOk();

    public static NotOk getInstance() {
      return instance;
    }

    @Override
    public String toString() {
      return "NotOk";
    }
  };
}
