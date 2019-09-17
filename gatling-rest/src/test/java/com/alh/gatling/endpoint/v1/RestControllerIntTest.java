/*
 *
 *   Copyright 2016 alh Technology
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

package com.alh.gatling.endpoint.v1;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;

import com.alh.gatling.AbstractRestIntTest;
import com.alh.gatling.domain.WorkerModel;

/**
 * Trivial integration test class that exercises the Junit spring runner and in container testing.
 *
 * @author
 *
 */
public class RestControllerIntTest extends AbstractRestIntTest {
    private final Logger log = LoggerFactory.getLogger(RestControllerIntTest.class);

    @Value("${security.username}") private String USERNAME;
    @Value("${security.password}") private String PASSWORD;

    @Before
    public void setup() {
        BasicAuthorizationInterceptor bai = new BasicAuthorizationInterceptor(USERNAME, PASSWORD);
        template.getRestTemplate().getInterceptors().add(bai);
    }

    @Test
    public void test(){
        WorkerModel[] info = template.getForObject(rootUrl+ "/server/info", WorkerModel[].class);
        log.debug(info.toString());
    }

}
