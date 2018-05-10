/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.pnc.metrics;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import lombok.Getter;
import org.jboss.pnc.metrics.exceptions.NoPropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@Singleton
@Startup
public class MetricsConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MetricsConfiguration.class);

    private static final String METRIC_JVM_MEMORY = "jvm.memory";
    private static final String METRIC_JVM_GARBAGE = "jvm.garbage";
    private static final String METRIC_JVM_THREADS = "jvm.threads";
    private static final String METRIC_JVM_CLASSLOADING = "jvm.classloading";

    private static final String  GRAPHITE_SERVER_KEY = "metrics_graphite_server";
    private static final String  GRAPHITE_PORT_KEY = "metrics_graphite_port";
    private static final String  GRAPHITE_PREFIX_KEY = "metrics_graphite_prefix";

    // The interval is in seconds
    private static final String  GRAPHITE_INTERVAL_KEY = "metrics_graphite_interval";
    private static final int DEFAULT_GRAPHITE_INTERVAL = 60;

    @Getter
    private MetricRegistry metricRegistry;

    @PostConstruct
    public void init() {

        metricRegistry = new MetricRegistry();

        monitorJvmMetrics();
        setupGraphiteReporter();

    }

    /**
     * If propertyName has no value (either specified in system property or environment property), then just return
     * the default value. System property value has priority over environment property value.
     * <p>
     * If value can't be parsed, just return the default value.
     *
     * @param propertyName property name to check the value
     * @param description  description to print in case value can't be parsed as an integer
     * @return value from property, or throws NoPropertyException if property not specified
     * @throws NoPropertyException if property not specified on system or env value
     */
    private String getValueFromProperty(String propertyName, String description) throws NoPropertyException {

        String value;

        String valueSys = System.getProperty(propertyName);
        String valueEnv = System.getenv(propertyName);

        if (valueSys != null) {
            value = valueSys;
        } else if (valueEnv != null) {
            value = valueEnv;
        } else {
            throw new NoPropertyException("Property '" + propertyName + "' not specified");
        }

        logger.info("Updated " + description + " to: " + value);

        return value;
    }


    /**
     * Add metrics for JVM. Already provided by metrics-jvm
     */
    private void monitorJvmMetrics() {

        logger.info("Registering JVM metrics");

        metricRegistry.register(METRIC_JVM_GARBAGE, new GarbageCollectorMetricSet());
        metricRegistry.register(METRIC_JVM_MEMORY, new MemoryUsageGaugeSet());
        metricRegistry.register(METRIC_JVM_THREADS, new ThreadStatesGaugeSet());
        metricRegistry.register(METRIC_JVM_CLASSLOADING, new ClassLoadingGaugeSet());

    }

    /**
     * Read system or environment variable to know to which Graphite server to report data to.
     *
     * If any of the required variables are not specified, abandon the setup and warn the user.
     *
     */
    private void setupGraphiteReporter() {

        int graphiteInterval = DEFAULT_GRAPHITE_INTERVAL;

        try {

            graphiteInterval = Integer.parseInt(
                    getValueFromProperty(GRAPHITE_INTERVAL_KEY, "Graphite Interval reporting"));

        } catch (NumberFormatException e) {

            // thrown because we couldn't parse the interval as a number
            logger.warn("Could not parse Graphite interval! Using default value of {} seconds instead", DEFAULT_GRAPHITE_INTERVAL);

        } catch(NoPropertyException e) {
            // If we're here that property is not specified. Just continue using the default
        }

        try {

            String graphiteServer = getValueFromProperty(GRAPHITE_SERVER_KEY, "Graphite Server URL");
            int graphitePort = Integer.parseInt(getValueFromProperty(GRAPHITE_PORT_KEY, "Graphite Port"));
            String graphitePrefix = getValueFromProperty(GRAPHITE_PREFIX_KEY, "Graphite Prefix");

            startGraphiteReporter(graphiteServer, graphitePort, graphitePrefix, graphiteInterval);

        } catch (NumberFormatException e) {

            // thrown because it couldn't parse graphitePort
            logger.warn("Could not parse Graphite port! Aborting reporting data to Graphite", e);

        } catch (NoPropertyException e) {

            logger.warn("Could not find property required to setup Graphite reporting! Reason: {}", e.getMessage());

        }
    }

    /**
     * Reporter of metrics to a Graphite server
     *
     * @param host   Graphite server
     * @param port   Graphite port (usually 2003)
     * @param prefix Prefix to use as a namespace for our logs
     * @param interval interval at which to report metrics to Graphite in seconds
     */
    private void startGraphiteReporter(String host, int port, String prefix, int interval) {

        logger.info("Setting up Graphite reporter");

        Graphite graphite = new Graphite(new InetSocketAddress(host, port));

        GraphiteReporter reporter = GraphiteReporter.forRegistry(metricRegistry)
                .prefixedWith(prefix)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .build(graphite);

        reporter.start(interval, TimeUnit.SECONDS);
    }
}
