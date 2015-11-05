package com.walmart.store.location;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jersey2.InstrumentedResourceMethodApplicationListener;
import com.walmart.store.location.endpoint.v1.LocationController;

/**
 * This class configures Jersey in Spring and registers Jersey endpoints.
 * 
 * @author jevans
 *
 */
@Configuration
@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {
	private static final Logger logger = LoggerFactory.getLogger(JerseyConfig.class);
	
	private final MetricRegistry registry = new MetricRegistry();
	
	/**
	 * If we built it, then Spring will use our MetricRegistry instead of creating its own.
	 * @return our registry instance
	 */
	@Bean
	public MetricRegistry metricRegistry() { return registry; };
	
    public JerseyConfig() {
    	logger.info("Registering MetricRegistry with Jersey.");
    	register(new InstrumentedResourceMethodApplicationListener(registry));
    	
    	registerControllers();
    }
    
    protected void registerControllers() {
        register(LocationController.class);
    }
    
}
