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
package org.jboss.pnc.client;

import org.jboss.pnc.common.log.MDCUtils;
import org.jboss.pnc.common.util.StringUtils;
import org.slf4j.MDC;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.Map;

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
