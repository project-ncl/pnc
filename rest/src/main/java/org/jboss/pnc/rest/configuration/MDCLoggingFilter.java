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

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class MDCLoggingFilter implements ContainerRequestFilter {

    private Logger logger = LoggerFactory.getLogger(MDCLoggingFilter.class);

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        String logContext = containerRequestContext.getHeaderString("log-context");
        if (logContext == null) {
            logContext = RandomUtils.randString(12);
        }
        MDCUtils.clear();
        MDCUtils.addRequestContext(logContext);
        logger.info("Log context {} for request {}.", logContext, containerRequestContext.getUriInfo().getPath());
    }
}
