package com.walmart.store.location;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.Assert.assertEquals;

/**
 * Abstract helper class that defines necessary web integration testing annotations and common functions.
 * 
 * @author jevans
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest
@ActiveProfiles({"testing"})
public abstract class AbstractRestIntTest {
	
	protected RestTemplate template = new TestRestTemplate();

	@Autowired
	protected ObjectMapper mapper;

	protected String toJson(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new RuntimeException("Error marshalling object '" + obj + "' to json string.", e);
		}
	}
	
	protected void assertJsonEquals(Object obj, Object obj2) {
		assertEquals("Objects not equal:  " + obj + ", " + obj2, toJson(obj), toJson(obj2));
	}
	
}
