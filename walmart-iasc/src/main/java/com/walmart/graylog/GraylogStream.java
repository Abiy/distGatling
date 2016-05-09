package com.walmart.graylog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by walmart
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraylogStream implements Serializable{
    private int total;
    private List<Stream> streams;

    public GraylogStream() {
        streams = new ArrayList<>();
        total = 0;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Stream> getStreams() {
        return streams;
    }

    public void setStreams(List<Stream> streams) {
        this.streams = streams;
    }

    public Optional<Stream> findStream(String title){
        Optional<Stream> first = streams.stream().filter(s->s.getTitle().equalsIgnoreCase(title)).findFirst();
        return first;
    }
}
