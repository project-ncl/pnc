/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.remotecoordinator.rexclient.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jboss.pnc.api.constants.HttpHeaders.AUTHORIZATION_STRING;

@Provider
public class LoggingFilter implements ClientRequestFilter, ClientResponseFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    /*
     * Useful stuff
     * 
     * @Context UriInfo info;
     * 
     * @Context HttpServerRequest request;
     */

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        MultivaluedMap<String, String> headers = requestContext.getStringHeaders();
        log.debug(
                "Rex client request: {} {} Headers: {}.",
                requestContext.getMethod(),
                requestContext.getUri(),
                toString(headers));
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        log.debug("Rex client response status: {}.", responseContext.getStatus());
    }

    public static String toString(MultivaluedMap<String, String> map) {
        return map.entrySet().stream().map(LoggingFilter::sanitized).collect(Collectors.joining("; "));
    }

    private static String sanitized(Map.Entry<String, List<String>> entry) {
        if (AUTHORIZATION_STRING.equals(entry.getKey())) {
            return entry.getKey() + ":***";
        } else {
            return entry.getKey() + ":" + entry.getValue();
        }
    }
}
