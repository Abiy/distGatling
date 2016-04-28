package com.walmart.grafana;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by walmart
 */
@XmlRootElement
public class Target {
    private String refId;
    private String target;

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "Target{" +
                "refId='" + refId + '\'' +
                ", target='" + target + '\'' +
                '}';
    }
}
