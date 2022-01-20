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
package org.jboss.pnc.rest.endpoints.internal;

import org.jboss.pnc.facade.providers.api.HealthCheckProvider;
import org.jboss.pnc.rest.endpoints.internal.api.HealthCheckEndpoint;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.Map;

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
