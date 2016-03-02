package com.walmart.gatling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplateWithPutReturnSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

	private static final Logger logger = LoggerFactory.getLogger(AbstractRestIntTest.class);
	protected RestTemplateWithPutReturnSupport template = new RestTemplateWithPutReturnSupport();
	protected final String rootUrl = "http://localhost:8080/gatling";

	@Autowired
	protected ObjectMapper mapper;

	@BeforeClass
	public static void disableCCM() {
		System.setProperty("com.walmart.platform.config.runOnEnv", "testing");
		System.setProperty("com.walmart.platform.config.appName", "walmart-container-service");
		System.setProperty("scm.server.access.enabled", "false");
		System.setProperty("scm.snapshot.enabled", "false");
	}

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

	protected <T> T getForObject(String url, Class<T> responseType) {
		return getForObject(url, responseType, HttpStatus.OK);
	}

	protected void delete(String url, HttpStatus status) {
		ResponseEntity<Void> response = template.exchange(url, HttpMethod.DELETE, null, Void.class);
		HttpStatus code = response.getStatusCode();
		assertEquals(status, code);
	}

	protected HttpStatus delete(String url) {
		ResponseEntity<Void> response = template.exchange(url, HttpMethod.DELETE, null, Void.class);
		HttpStatus code = response.getStatusCode();
		if(code == HttpStatus.OK || code == HttpStatus.NO_CONTENT || code == HttpStatus.NOT_FOUND)
			return response.getStatusCode();
		else {
			fail("Expected the delete response to be 200 or 404, but was " + code.value() + "(" + code.getReasonPhrase() + ").");
			return null; //for compiler
		}
	}

	protected <T> T getForObject(String url, Class<T> responseType,
								 HttpStatus expectedStatus) {
		ResponseEntity<T> entity = template.getForEntity(url, responseType);
		assertEquals(expectedStatus, entity.getStatusCode());
		return entity.getBody();
	}

	protected <T> T putForObject(String url, Object input, Class<T> responseType,
								 HttpStatus expectedStatus) {
		ResponseEntity<T> entity = template.putForEntity(url, input, responseType);
		if(expectedStatus != entity.getStatusCode()) {
			if(entity.hasBody())
				logger.debug("Error processings put:  {}", entity.getBody().toString());
		}

		assertEquals(expectedStatus, entity.getStatusCode());

		return entity.getBody();
	}

	protected <T> T postForObject(String url, Object post, Class<T> responseType,
								  HttpStatus expectedStatus) {
		ResponseEntity<T> entity = template.postForEntity(url, post, responseType);
		assertEquals(expectedStatus, entity.getStatusCode());
		return entity.getBody();
	}
}
