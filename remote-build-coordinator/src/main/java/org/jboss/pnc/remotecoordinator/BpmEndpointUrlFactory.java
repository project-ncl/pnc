/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator;

import java.net.URI;
import java.util.List;

import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.common.Strings;

public class BpmEndpointUrlFactory {

    /**
     * Base url of BPM. It shouldn't end with a trailing slash
     */
    private final String baseUrl;

    public BpmEndpointUrlFactory(String baseUrl) {
        this.baseUrl = Strings.stripEndingSlash(baseUrl);
    }

    public Request startProcessInstance(
            String deploymentId,
            String processId,
            String correlationKey,
            List<Request.Header> headers,
            Object attachment) {
        return new Request(
                Request.Method.POST,
                URI.create(
                        baseUrl + "/containers/" + deploymentId + "/processes/" + processId + "/instances/correlation/"
                                + correlationKey),
                headers,
                attachment);
    }

    public Request processInstanceSignalByCorrelation(
            String deploymentId,
            String correlationKey,
            String signal,
            List<Request.Header> headers) {
        return new Request(
                Request.Method.POST,
                URI.create(
                        baseUrl + "/containers/" + deploymentId + "/processes/instances/correlation/" + correlationKey
                                + "/signal/" + signal),
                headers);
    }
}
