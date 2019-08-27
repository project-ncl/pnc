/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest;

import org.apache.commons.io.IOUtils;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.common.util.MapUtils;
import org.jboss.pnc.common.util.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RequestLoggingFilter implements ContainerRequestFilter {

    private Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        String logContext = context.getHeaderString("log-context");
        if (logContext == null) {
            logContext = RandomUtils.randString(12);
        }
        MDCUtils.clear();
        MDCUtils.addRequestContext(logContext);
        UriInfo uriInfo = context.getUriInfo();
        Request request = context.getRequest();
        logger.info("Log context {} for request: {} {}", logContext, request.getMethod(), uriInfo.getRequestUri());
        if (logger.isTraceEnabled()) {
            MultivaluedMap<String, String> headers = context.getHeaders();
            logger.trace("Headers: " + MapUtils.toString(headers));
            logger.trace("Entity: {}.", getEntityBody(context));
            logger.trace("User principal name: {}", getUserPrincipalName(context));
        }
    }

    private String getUserPrincipalName(ContainerRequestContext context) {
        SecurityContext securityContext = context.getSecurityContext();
        if (securityContext != null) {
            Principal userPrincipal = securityContext.getUserPrincipal();
            if (userPrincipal != null) {
                return userPrincipal.getName();
            } else {
                return "-- there is no userPrincipal --";
            }
        } else {
            return "-- there is no securityContext --";
        }
    }

    private String getEntityBody(ContainerRequestContext requestContext) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = requestContext.getEntityStream();

        final StringBuilder b = new StringBuilder();
        try {
            IOUtils.copy(in, out);

            byte[] requestEntity = out.toByteArray();
            if (requestEntity.length == 0) {
                b.append("\n");
            } else {
                b.append(new String(requestEntity)).append("\n");
            }
            requestContext.setEntityStream( new ByteArrayInputStream(requestEntity) );

        } catch (IOException e) {
            logger.error("Error logging REST request.", e);
        }
        return b.toString();
    }
}
