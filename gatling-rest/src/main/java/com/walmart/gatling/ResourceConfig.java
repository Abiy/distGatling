package com.walmart.gatling;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by walmart
 */
@Configuration
public class ResourceConfig extends WebMvcConfigurerAdapter {

    @Value("${job.logDirectory}")
    private String logDirectory;


    /**
     * registers the reports path as an http resource directory using /resources
     * this allows us to serve the gatling reports via http
     * @param registry
     */
    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("file:"+logDirectory + "reports/");
    }
}
