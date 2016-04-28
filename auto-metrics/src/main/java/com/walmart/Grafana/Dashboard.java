package com.walmart.grafana;

import java.util.List;

/**
 * Created by walmart
 */
public class Dashboard {
    private boolean editable;
    private boolean hideControls;
    private boolean sharedCrosshair;
    private Integer id;
    private String[] links;
    private String[] tags;
    private int version;
    private String title;
    private String style;
    private int schemaVersion = 8;

    private List<Row> rows;
}
