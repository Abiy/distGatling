package com.walmart.graylog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.typesafe.config.Config;

/**
 * Created by walmart
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertConditions {

    private String type;
    private Parameters parameters;

    public AlertConditions(Config config) {
        this.type = config.getString("type");
        this.parameters = new Parameters();
        parameters.setBacklog(config.getInt("backlog"));
        parameters.setGrace(config.getInt("grace"));
        parameters.setThreshold(config.getInt("threshold"));
        parameters.setTime(config.getInt("time"));
        parameters.setThreshold_type(config.getString("threshold_type"));

    }

    public AlertConditions() {
        //for jackson
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    public static class Parameters{
        private int grace;
        private int threshold;
        private String threshold_type;
        private int backlog;
        private int time;

        public int getGrace() {
            return grace;
        }

        public void setGrace(int grace) {
            this.grace = grace;
        }

        public int getThreshold() {
            return threshold;
        }

        public void setThreshold(int threshold) {
            this.threshold = threshold;
        }

        public String getThreshold_type() {
            return threshold_type;
        }

        public void setThreshold_type(String threshold_type) {
            this.threshold_type = threshold_type;
        }

        public int getBacklog() {
            return backlog;
        }

        public void setBacklog(int backlog) {
            this.backlog = backlog;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }
    }
}
