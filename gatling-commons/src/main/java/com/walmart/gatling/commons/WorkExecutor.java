package com.walmart.gatling.commons;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public abstract class WorkExecutor extends UntypedActor {
    protected LoggingAdapter log = Logging.getLogger(getContext().system(), this);
}
