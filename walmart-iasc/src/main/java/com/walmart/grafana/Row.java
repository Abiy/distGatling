package com.walmart.grafana;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by walmart
 */
@XmlRootElement
public class Row {

    private boolean editable;
    private boolean showTitle;
    private String title;
    private List<Panel> panels;
}
