package com.walmart.store.location;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jersey2.InstrumentedResourceMethodApplicationListener;
import com.walmart.store.location.endpoint.v1.LocationController;

import javax.ws.rs.ApplicationPath;

@Configuration
@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {
	
    public JerseyConfig() {
    	register(new InstrumentedResourceMethodApplicationListener(new MetricRegistry()));
        register(LocationController.class);
    }
    
}
