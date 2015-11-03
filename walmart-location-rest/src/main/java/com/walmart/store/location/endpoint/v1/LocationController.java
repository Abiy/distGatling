package com.walmart.store.location.endpoint.v1;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

/**
 * Created by ahailem on 11/2/15.
 */
@Component
@Path("/home")
public class LocationController {

    private final Logger log = LoggerFactory.getLogger(LocationController.class);

    @GET
    @Produces("application/json")
    public Map<String, Object> home() {
        log.info("Processing home request.");

        return ImmutableMap.of(
       		"name", "someName",
                "age", 27
        );
    }

}
