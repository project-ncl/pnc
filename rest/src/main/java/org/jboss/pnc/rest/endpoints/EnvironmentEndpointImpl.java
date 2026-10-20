/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.endpoints;

import static org.jboss.pnc.facade.providers.api.UserRoles.*;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.pnc.dto.Environment;
import org.jboss.pnc.dto.requests.EnvironmentDeprecationRequest;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.EnvironmentProvider;
import org.jboss.pnc.rest.api.endpoints.EnvironmentEndpoint;
import org.jboss.pnc.rest.api.parameters.PageParameters;

@ApplicationScoped
@RolesAllowed({ USERS, USERS_ADMIN })
public class EnvironmentEndpointImpl implements EnvironmentEndpoint {

    @Inject
    private EnvironmentProvider environmentProvider;

    private EndpointHelper<Integer, Environment, Environment> endpointHelper;

    @PostConstruct
    public void init() {
        endpointHelper = new EndpointHelper<>(Environment.class, environmentProvider);
    }

    @Override
    public Page<Environment> getAll(PageParameters pageParameters) {
        return endpointHelper.getAll(pageParameters);
    }

    @Override
    public Environment getSpecific(String id) {
        return endpointHelper.getSpecific(id);
    }

    @Override
    @RolesAllowed({ USERS_ENVIRONMENT_ADMIN, USERS_ADMIN })
    public Environment createNew(Environment environment) {
        return endpointHelper.create(environment);
    }

    @Override
    @RolesAllowed({ USERS_ENVIRONMENT_ADMIN, USERS_ADMIN })
    public Environment deprecate(String id, EnvironmentDeprecationRequest request) {
        return environmentProvider.deprecateEnvironment(id, request.getReplacementEnvironmentId());
    }

}
