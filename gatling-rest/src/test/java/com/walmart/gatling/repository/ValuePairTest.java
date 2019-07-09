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

package com.walmart.gatling.repository;

import com.walmart.gatling.domain.WorkerModel;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *  on 5/3/17.
 */
public class ValuePairTest {

    @Test
    public void testRoleExtractor(){
        String path = "akka.tcp://PerformanceSystem@10.165.150.120:2555/user/script4#-1964952736";
        WorkerModel pair = new WorkerModel("Idle",path,"workerId");
        System.out.println(pair.getRole());
        assertEquals("script", pair.getRole().replaceAll("[0-9]", ""));
    }

}