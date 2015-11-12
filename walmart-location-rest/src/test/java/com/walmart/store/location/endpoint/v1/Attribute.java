package com.walmart.store.location.endpoint.v1;

/**
 *
 */
public class Attribute {
    private String validationType;
    private String type;
    private String name;
    private boolean required;
    private String expression;
    private String[] metaData;
    private String defaultValue;

    public Attribute(String name,String type, boolean required, String expression, String[] metaData) {
        this.name = name;
        this.required = required;
        this.expression = expression;
        this.metaData = metaData;
        this.type = type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String[] getMetaData() {
        return metaData;
    }

    public void setMetaData(String[] metaData) {
        this.metaData = metaData;
    }

    public String getValidationType() {
        return validationType;
    }

    public void setValidationType(String validationType) {
        this.validationType = validationType;
    }
}
