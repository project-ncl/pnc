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
package org.jboss.pnc.rest.configuration;

import org.jboss.pnc.common.mdc.MDCUtils;
import org.jboss.pnc.common.util.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class MDCLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private Logger logger = LoggerFactory.getLogger(MDCLoggingFilter.class);

    private static final String REQUEST_EXECUTION_START = "request-execution-start";;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        containerRequestContext.setProperty(REQUEST_EXECUTION_START, System.currentTimeMillis());

        String logContext = containerRequestContext.getHeaderString("log-context");
        if (logContext == null) {
            logContext = RandomUtils.randString(12);
        }
        MDCUtils.clear();
        MDCUtils.addRequestContext(logContext);
        logger.info("Requested {}.", containerRequestContext.getUriInfo().getPath());
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        Long startTime = (Long) containerRequestContext.getProperty(REQUEST_EXECUTION_START);

        String took;
        if (startTime == null) {
            took="-1";
        } else {
            took = Long.toString(System.currentTimeMillis() - startTime);
        }

        try (
                MDC.MDCCloseable mdcTook = MDC.putCloseable("request.took", took);
                MDC.MDCCloseable mdcStatus = MDC.putCloseable("response.status",
                        Integer.toString(containerResponseContext.getStatus()));
        ) {
            logger.debug("Completed {}.", containerRequestContext.getUriInfo().getPath());
        }
    }
}
