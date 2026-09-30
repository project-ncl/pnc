/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.rexclient;

import java.util.List;
import java.util.Map;

import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import org.jboss.pnc.api.constants.HttpHeaders;
import org.jboss.pnc.auth.ServiceAccountClient;
import org.jboss.pnc.common.log.MDCUtils;

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

        ServiceAccountClient serviceAccountClient = CDI.current().select(ServiceAccountClient.class).get();
        outgoingHeaders.put(HttpHeaders.AUTHORIZATION_STRING, List.of(serviceAccountClient.getAuthHeaderValue()));
        addMDCHeaders(outgoingHeaders);

        return outgoingHeaders;
    }

    private void addMDCHeaders(MultivaluedMap<String, String> outgoingHeaders) {
        Map<String, String> allMDCValues = MDCUtils.getHeadersFromMDC();

        allMDCValues.forEach(outgoingHeaders::putSingle);
    }
}
