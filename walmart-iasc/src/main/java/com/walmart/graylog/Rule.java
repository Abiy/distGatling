package com.walmart.graylog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by walmart
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Rule implements Serializable {
    private String field;
    private String type;
    private String inverted;
    private String value;

    public Rule(String field, String type, String inverted, String value) {
        this.field = field;
        this.type = type;
        this.inverted = inverted;
        this.value = value;
    }

    public Rule() {
    }


    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInverted() {
        return inverted;
    }

    public void setInverted(String inverted) {
        this.inverted = inverted;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
