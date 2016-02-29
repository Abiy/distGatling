package com.walmart.store.container;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jersey2.InstrumentedResourceMethodApplicationListener;

import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;

/**
 * This class configures Jersey in Spring and registers annotated Jersey endpoints.
 * 
 * @author jevans
 *
 */
@Component
@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {
	private static final Logger logger = LoggerFactory.getLogger(JerseyConfig.class);
	
	private final MetricRegistry registry = new MetricRegistry();
	
	/**
	 * If we built it, then Spring will use our MetricRegistry instead of creating its own.
	 * @return our registry instance
	 * @see org.springframework.boot.actuate.autoconfigure.MetricRepositoryAutoConfiguration
	 */
	@Bean
	public MetricRegistry metricRegistry() { return registry; };

	/**
	 * Constructor.
	 * @param controllerPackages the base package(s) containing annotated JerseyControllers.
	 */
	@Autowired
    public JerseyConfig(@Value("${jersey.controller.packages}") String[] controllerPackages) {
    	registerMetricRegistry();
    	registerControllers(controllerPackages);
    }

	/**
	 * Called during construction to {@link #register(Object)} the {@link com.codahale.metrics.jersey2.InstrumentedResourceMethodApplicationListener}
	 * using our Jersey singleton MetricRegistry instance.
	 */
	protected void registerMetricRegistry() {
		logger.info("Registering MetricRegistry with Jersey.");
    	register(new InstrumentedResourceMethodApplicationListener(registry));
	}
    
	/**
	 * Scans for {@link javax.ws.rs.Path} annotated classes in the given packages and registers them with Jersey.
	 * @param controllerPackages Jersery controller base package names
	 */
    protected void registerControllers(String[] controllerPackages) {
        ClassPathScanningCandidateComponentProvider scanner =
	       		new ClassPathScanningCandidateComponentProvider(false);
	   		scanner.addIncludeFilter(new AnnotationTypeFilter(javax.ws.rs.Path.class));

	   	for(String controllerPackage : controllerPackages) {
	   		logger.info("Scanning for Jersey controllers in '{}' package.", controllerPackage);
	   		
	        for (BeanDefinition bd : scanner.findCandidateComponents(controllerPackage)) {
	        	logger.info("Registering Jersey endpoint class:  {}", bd.getBeanClassName());
	        	Class<?> controllerClazz = getJerseyControllerClass(bd.getBeanClassName());
	        	if(controllerClazz != null)
	        		register(controllerClazz);
	        }
    	}
    }
    
    /**
     * Retrieve a class instance for the given class name, by default uses the ContextClassLoader.
     * 
     * @param className controller class name
     * @return controller class instance
     */
    protected Class<?> getJerseyControllerClass(String className) {
    	try {
			return Class.forName(className, true, Thread.currentThread().getContextClassLoader());
		} catch (ClassNotFoundException e) {
			logger.error("Error loading class '{}' from the context class loader.", className);
			return null;
		}
    }
    
}
