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
package org.jboss.pnc.rest;

import org.jboss.pnc.common.Strings;
import org.jboss.pnc.facade.util.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

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

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getRequest().getMethod().toUpperCase();
        String path = requestContext.getUriInfo().getPath();

        if ((path.matches("/builds/ssh-credentials.*") || path.matches("/users/current.*"))
                && Strings.anyStringEquals(method, HttpMethod.GET)
                && !isUserInAnyRole(UserService.ROLE_USER, UserService.ROLE_ADMIN, UserService.ROLE_SYSTEM_USER)) {
            throw new NotAuthorizedException("Authorization required to access this resource.");
        }
        if (path.matches("/.*")
                && Strings.anyStringEquals(
                        method,
                        HttpMethod.POST,
                        HttpMethod.PUT,
                        HttpMethod.DELETE,
                        HttpMethod.HEAD,
                        HttpMethod.PATCH)
                && !isUserInAnyRole(UserService.ROLE_USER, UserService.ROLE_ADMIN, UserService.ROLE_SYSTEM_USER)) {
            throw new NotAuthorizedException("Authorization required to access this resource.");
        }
    }

    private boolean isUserInAnyRole(String... roles) {
        if (!userService.isUserLoggedIn()) {
            return false;
        }
        for (String role : roles) {
            if (userService.hasLoggedInUserRole(role)) {
                return true;
            }
        }
        return false;
    }
}
