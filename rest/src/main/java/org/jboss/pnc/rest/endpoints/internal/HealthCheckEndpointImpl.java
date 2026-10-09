/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.endpoints.internal;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.jboss.pnc.facade.providers.api.HealthCheckProvider;
import org.jboss.pnc.rest.endpoints.internal.api.HealthCheckEndpoint;

@ApplicationScoped
public class HealthCheckEndpointImpl implements HealthCheckEndpoint {

    @Inject
    private HealthCheckProvider healthCheckProvider;

    @Override
    public Response check() {
        return generateResponseFromResult(healthCheckProvider.check());
    }

    private Response generateResponseFromResult(Map<String, Boolean> result) {
        if (result == null) {
            return Response.serverError().build();
        } else {

            boolean atLeastOneCheckFailed = result.values().stream().anyMatch(v -> !v);

            if (atLeastOneCheckFailed) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
            } else {
                return Response.ok(result).build();
            }
        }

    }
}
