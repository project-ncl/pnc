/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest;

import static org.jboss.pnc.facade.providers.api.UserRoles.USERS;
import static org.jboss.pnc.facade.providers.api.UserRoles.USERS_ADMIN;

import java.io.IOException;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import org.jboss.pnc.common.Strings;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.facade.util.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Provider
@PreMatching
@Priority(2)
public class SecurityConstraintFilter implements ContainerRequestFilter {

    private Logger logger = LoggerFactory.getLogger(SecurityConstraintFilter.class);
    private static final String REQUEST_EXECUTION_START = "request-execution-start";

    @Inject
    UserService userService;

    @Inject
    SystemConfig systemConfig;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getRequest().getMethod().toUpperCase();
        String path = requestContext.getUriInfo().getPath();

        if ((path.matches("/builds/ssh-credentials.*") || path.matches("/users/current.*"))
                && Strings.anyStringEquals(method, HttpMethod.GET) && !userService.isUserLoggedIn()) {
            throw new NotAuthorizedException("Authorization required to access this resource.");
        }
        if (path.matches("/.*")
                && Strings.anyStringEquals(method, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.PATCH)
                && !userService.isUserLoggedIn()) {
            throw new NotAuthorizedException("Authorization required to access this resource.");
        }
        if (systemConfig.isRequirePncUsersRoleForMutating()
                && Strings.anyStringEquals(method, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.PATCH)
                && !isInternalPath(path) && userService.isUserLoggedIn()
                && !(userService.hasLoggedInUserRole(USERS) || userService.hasLoggedInUserRole(USERS_ADMIN))) {
            throw new ForbiddenException("You must have the " + USERS + " role to perform this operation.");
        }
    }

    private static boolean isInternalPath(String path) {
        return path.startsWith("/build-tasks/") || path.startsWith("/bpm/") || path.startsWith("/debug/")
                || path.startsWith("/health") || path.matches("/deliverable-analyses/complete")
                || path.matches("/builds/[^/]+/brew-push/complete") || path.matches("/operations/[^/]+/complete");
    }
}
