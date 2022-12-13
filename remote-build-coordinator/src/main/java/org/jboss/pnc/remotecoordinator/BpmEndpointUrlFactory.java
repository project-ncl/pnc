/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.remotecoordinator;

import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.common.Strings;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;

import java.net.URI;
import java.util.List;

public class BpmEndpointUrlFactory {

    /**
     * Base url of BPM. It shouldn't end with a trailing slash
     */
    private final String baseUrl;

    public BpmEndpointUrlFactory(String baseUrl) {
        this.baseUrl = Strings.stripEndingSlash(baseUrl);
    }

    public Request startProcessInstance(String deploymentId, String processId, String correlationKey, List<Request.Header> headers, Object attachment) {
        return new Request(
                Request.Method.POST,
                URI.create(baseUrl + "/containers/" + deploymentId + "/processes/" + processId + "/instances/correlation/" + correlationKey),
                headers,
                JsonOutputConverterMapper.apply(attachment));
    }

    public Request processInstanceSignalByCorrelation(String deploymentId, String correlationKey, String signal, List<Request.Header> headers) {
        return new Request(
                Request.Method.POST,
                URI.create(baseUrl + "/containers/" + deploymentId + "/processes/instances/correlation/" + correlationKey + "/signal/" + signal),
                headers);
    }
}
