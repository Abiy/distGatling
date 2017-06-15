/*
 *
 *   Copyright 2016 Walmart Technology
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.walmart.gatling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Abstract helper class that defines necessary web integration testing annotations and common functions.
 * 
 * @author walmart
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest
@ActiveProfiles({"testing"})
public abstract class AbstractRestIntTest {

	private static final Logger logger = LoggerFactory.getLogger(AbstractRestIntTest.class);
	protected TestRestTemplate template = new TestRestTemplate();
	protected final String rootUrl = "http://localhost:8080/gatling";

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


	protected <T> T postForObject(String url, Object post, Class<T> responseType,
								  HttpStatus expectedStatus) {
		ResponseEntity<T> entity = template.postForEntity(url, post, responseType);
		assertEquals(expectedStatus, entity.getStatusCode());
		return entity.getBody();
	}
}
