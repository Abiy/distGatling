package com.walmart.store.location;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

import com.walmart.store.location.endpoint.v1.LocationController;

@Configuration
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        register(LocationController.class);
    }
}
