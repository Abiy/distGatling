package com.walmart.graylog;


import com.typesafe.config.Config;

/**
 * Created by walmart
 */
public class AlertReceiver {
    private String entity ;
    private String type ;
    private String streamId;

    public AlertReceiver() {
    }

    public AlertReceiver(Config config,String streamId) {
        this.entity = config.getString("entity");
        this.type = config.getString("type");
        this.streamId = streamId;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }
}
