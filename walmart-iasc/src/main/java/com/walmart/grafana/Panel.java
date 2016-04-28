package com.walmart.grafana;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by walmart
 */
@XmlRootElement
public class Panel {

    private String datasource;
    private boolean bars;
    private boolean editable;
    private boolean error;
    private boolean isNew;
    private boolean lines;
    private int lineWidth;
    private int fill;
    private Integer id;
    private List<Target> targets;
    private List<String> links;
    private String nullPointMode;
    private boolean percentage;
    private int pointradius;
    private boolean points   ;
    private String renderer;
    private int span;
    private boolean stack;
    private boolean steppedLine;
    private String title;
    private String type;
    @JsonProperty("y-axis")
    private boolean yaxis ;
    @JsonProperty("x-axis")
    private boolean xaxis ;
    @JsonProperty("y_formats")
    private String[] yFormats;
}
