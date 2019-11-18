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

package com.alh.gatling.config;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.alh.gatling.commons.HostUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Configured the metrics for collection and for reporting
 */
@Configuration
public class MonitoringConfig {

    @Bean
    public Graphite graphite(@Value("${graphite.host}")  String graphiteHost,
                             @Value("${graphite.port}")  int graphitePort) {
        return new Graphite(
                new InetSocketAddress(graphiteHost, graphitePort));
    }

    //@Bean
    public GraphiteReporter graphiteReporter(Graphite graphite,
                                             MetricRegistry registry,
                                             @Value("${graphite.prefix}")
    										 String prefix,@Value("${graphite.frequency-in-seconds}") long frequencyInSeconds) {
        GraphiteReporter reporter =
                GraphiteReporter.forRegistry(registry)
                        .prefixedWith(prefix + "." + HostUtils.lookupHost())
                        .convertRatesTo(TimeUnit.SECONDS)
                        .convertDurationsTo(TimeUnit.MILLISECONDS)
                        .filter(MetricFilter.ALL)
                        .build(graphite);
        reporter.start(frequencyInSeconds, TimeUnit.SECONDS);

        return reporter;
    }

    //@Bean
    public ConsoleReporter consoleReporter( MetricRegistry registry ) {
        ConsoleReporter reporter = ConsoleReporter.forRegistry(registry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(10, TimeUnit.SECONDS);
        return reporter;
    }

    @Bean
    public MemoryUsageGaugeSet memoryUsageGaugeSet(MetricRegistry registry) {
        MemoryUsageGaugeSet memoryUsageGaugeSet =
                new MemoryUsageGaugeSet();
        registry.register("memory", memoryUsageGaugeSet);
        return memoryUsageGaugeSet;
    }
    @Bean
    public ThreadStatesGaugeSet
    threadStatesGaugeSet(MetricRegistry registry) {
        ThreadStatesGaugeSet threadStatesGaugeSet =
                new ThreadStatesGaugeSet();
        registry.register("threads", threadStatesGaugeSet);
        return threadStatesGaugeSet;
    }
}