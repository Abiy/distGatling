package com.walmart.graylog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.typesafe.config.Config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by walmart
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Stream implements Serializable {
    private String title;
    private String description;
    @JsonProperty("content_pack")
    private String content_pack;
    @JsonProperty("matching_type")
    private String matching_type;

    private List<Rule> rules;

    public Stream(Config config) {
        Config streamConfig = config.getConfig("graylog.stream");
        title = streamConfig.getString("title");
        description = streamConfig.getString("description");
        if(!streamConfig.getIsNull("content_pack"))
         content_pack = streamConfig.getString("content_pack");
        matching_type = streamConfig.getString("matching_type");

        rules = new ArrayList<>();
        List<? extends Config> rulesConfig = streamConfig.getConfigList("rules");
        for (Config rule : rulesConfig) {
            rules.add(new Rule(rule.getString("field"),rule.getString("type"),rule.getString("inverted"),rule.getString("value")));
        }
    }

    public Stream( ) {
        //for jackson
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent_pack() {
        return content_pack;
    }

    public void setContent_pack(String content_pack) {
        this.content_pack = content_pack;
    }

    public String getMatching_type() {
        return matching_type;
    }

    public void setMatching_type(String matching_type) {
        this.matching_type = matching_type;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }
}
