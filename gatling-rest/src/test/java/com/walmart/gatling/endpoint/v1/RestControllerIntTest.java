package com.walmart.gatling.endpoint.v1;

import com.walmart.gatling.AbstractRestIntTest;
import com.walmart.gatling.repository.ValuePair;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Trivial integration test class that exercises the Junit spring runner and in container testing.
 * 
 * @author walmart
 *
 */
public class RestControllerIntTest extends AbstractRestIntTest {
    private final Logger log = LoggerFactory.getLogger(RestControllerIntTest.class);

    @Test
    public void test(){
        ValuePair[] info = template.getForObject(rootUrl+ "/server/info", ValuePair[].class);
        log.debug(info.toString());
    }
	
}
