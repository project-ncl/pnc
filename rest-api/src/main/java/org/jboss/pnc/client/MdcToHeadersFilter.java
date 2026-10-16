/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.client;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;

import org.jboss.pnc.common.util.StringUtils;
import org.slf4j.MDC;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class MdcToHeadersFilter implements ClientRequestFilter {

    private Map<String, String> mappings;

    public MdcToHeadersFilter(Map<String, String> mappings) {
        this.mappings = mappings;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        MultivaluedMap<String, Object> headers = requestContext.getHeaders();

        Map<String, String> context = MDC.getCopyOfContextMap();
        if (context == null) {
            return;
        }
        for (Map.Entry<String, String> mdcKeyHeaderKey : mappings.entrySet()) {
            String mdcValue = context.get(mdcKeyHeaderKey.getKey());
            // [NCL-7890] if the headers map already contains the mdc header specified in the request, don't add the
            // mdc key value to it. For e.g, traceparent header might already be specified from the Quarkus Otel
            // integration
            if (!StringUtils.isEmpty(mdcValue) && !headers.containsKey(mdcKeyHeaderKey.getValue())) {
                headers.add(mdcKeyHeaderKey.getValue(), mdcValue);
            }
        }
    }
}
