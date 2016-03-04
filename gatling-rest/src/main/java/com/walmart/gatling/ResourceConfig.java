package com.walmart.gatling;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created
 */
@Configuration
public class ResourceConfig extends WebMvcConfigurerAdapter {

    @Value("${job.logDirectory}")
    private String logDirectory;

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("file:"+logDirectory);
    }
}
