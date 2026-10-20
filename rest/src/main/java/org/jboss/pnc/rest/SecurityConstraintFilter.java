/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest;

import java.io.IOException;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.facade.util.UserService;

/**
 * Post-matching JAX-RS filter that enforces authentication (401). Role-based authorization (403) is handled by
 * RESTEasy's built-in SecurityInterceptor via the {@code resteasy.role.based.security} context parameter in web.xml.
 */
@Provider
@Priority(10)
public class SecurityConstraintFilter implements ContainerRequestFilter {

    private static final String VERSION_ENDPOINT_PATH = "/version";

    @Inject
    UserService userService;

    @Inject
    SystemConfig systemConfig;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if ("NO_AUTH".equals(systemConfig.getAuthenticationProviderId())) {
            return;
        }

        String path = requestContext.getUriInfo().getPath();
        if (path.equals(VERSION_ENDPOINT_PATH) || path.equals(VERSION_ENDPOINT_PATH + "/")) {
            return;
        }

        if (!userService.isUserLoggedIn()) {
            throw new NotAuthorizedException("Authorization required to access this resource.");
        }
    }
}