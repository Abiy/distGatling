package com.walmart.store.location.endpoint.v1;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by ahailem on 11/2/15.
 */

@RestController
public class LocationController {

    private final Logger log = LoggerFactory.getLogger(LocationController.class);

    @RequestMapping("/home")
    @ResponseBody
    public Map home() {
        log.info("Got other request");

        return ImmutableMap.of(
                "name", "someName",
                "age", 27
        );
    }

}
