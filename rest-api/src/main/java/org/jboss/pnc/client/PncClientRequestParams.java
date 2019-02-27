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
package org.jboss.pnc.client;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 2/26/19
 */
public class PncClientRequestParams implements ClientRequestFilter {
    private static final ThreadLocal<Map<String, String>> params = new ThreadLocal<>();

    /**
     * Set query parameters for a single request
     * @param params a map of query parameters to send
     */
    public static void setParams(Map<String, String> params) {
        PncClientRequestParams.params.set(params);
    }

    @Override
    public void filter(ClientRequestContext requestContext) {
        Map<String, String> paramMap = params.get();
        try {
            if (paramMap != null) {
                URI uri = requestContext.getUri();

                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    uri = UriBuilder.fromUri(uri)
                            .queryParam(param.getKey(), param.getValue())
                            .build();
                }
                requestContext.setUri(uri);
            }
        } finally {
            params.remove();
        }
    }
}
