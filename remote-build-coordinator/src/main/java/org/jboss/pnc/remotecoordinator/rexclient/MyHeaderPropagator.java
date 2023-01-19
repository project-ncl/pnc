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
package org.jboss.pnc.remotecoordinator.rexclient;

import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import org.jboss.pnc.api.constants.HttpHeaders;
import org.jboss.pnc.common.log.MDCUtils;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Map;

public class MyHeaderPropagator implements ClientHeadersFactory {

    /**
     * Uses headers from initial REST request to Orchestrator from a user and propagates them to Rest Client
     *
     * @param incomingHeaders - the map of headers from the inbound JAX-RS request. This will be an empty map if the
     *        associated client interface is not part of a JAX-RS request.
     * @param clientOutgoingHeaders - the read-only map of header parameters specified on the client interface.
     * @return the map of additional headers in addition to clientOutgoindHeaders
     */
    @Override
    public MultivaluedMap<String, String> update(
            MultivaluedMap<String, String> incomingHeaders,
            MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> outgoingHeaders = new MultivaluedHashMap<>();

        // propagate User Token if present in original request
        if (incomingHeaders.containsKey(HttpHeaders.AUTHORIZATION_STRING)) {
            outgoingHeaders
                    .put(HttpHeaders.AUTHORIZATION_STRING, incomingHeaders.get(HttpHeaders.AUTHORIZATION_STRING));
        }

        addMDCHeaders(outgoingHeaders);

        return outgoingHeaders;
    }

    private void addMDCHeaders(MultivaluedMap<String, String> outgoingHeaders) {
        Map<String, String> allMDCValues = MDCUtils.getHeadersFromMDC();

        allMDCValues.forEach(outgoingHeaders::putSingle);
    }
}
