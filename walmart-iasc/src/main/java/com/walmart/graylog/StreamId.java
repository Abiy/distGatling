package com.walmart.graylog;

/**
 * Created by walmart
 */
public class StreamId {
    private String stream_id;

    public String getStream_id() {
        return stream_id;
    }

    public void setStream_id(String stream_id) {
        this.stream_id = stream_id;
    }

    public boolean isValidStream(){
        return  getStream_id()!=null && getStream_id().length()>=0;
    }

    @Override
    public String toString() {
        return stream_id;
    }
}
