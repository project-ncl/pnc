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
package org.jboss.pnc.rest.configuration.metrics;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.jboss.pnc.metrics.MetricsConfiguration;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import static com.codahale.metrics.MetricRegistry.name;

@Provider
@Priority(Priorities.USER)
@TimedMetric
public class TimedMetricFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String METRICS_KEY = "pnc.rest";
    private static final String METRICS_RATE_KEY = ".rate";
    private static final String METRICS_TIMER_KEY = ".timer";

    @Context
    private ResourceInfo resourceInfo;

    @Inject
    private MetricsConfiguration metricsConfiguration;

    @Inject
    private HttpServletRequest servletRequest;


    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        MetricRegistry registry = metricsConfiguration.getMetricRegistry();

        String metricsName = METRICS_KEY +
                name(resourceInfo.getResourceClass().getSimpleName(), resourceInfo.getResourceMethod().getName());

        Timer timer = registry.timer(metricsName + METRICS_TIMER_KEY);
        Meter meter = registry.meter(metricsName + METRICS_RATE_KEY);

        Timer.Context counter = timer.time();
        meter.mark();

        servletRequest.setAttribute("timer.context.method", counter);

    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

        Timer.Context tc = (Timer.Context) servletRequest.getAttribute("timer.context.method");

        if (tc != null) {
            tc.stop();
            servletRequest.removeAttribute("timer.context.method");
        }
    }
}
