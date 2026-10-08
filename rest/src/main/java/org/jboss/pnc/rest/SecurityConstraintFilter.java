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

import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.facade.util.UserService;

import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Post-matching JAX-RS filter that enforces authentication (401) and role-based authorization (403). Reads
 * {@link RolesAllowed}, {@link PermitAll}, and {@link DenyAll} annotations from the matched resource method/class.
 */
@Provider
@Priority(10)
public class SecurityConstraintFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Inject
    UserService userService;

    @Inject
    SystemConfig systemConfig;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (!userService.isUserLoggedIn()) {
            throw new NotAuthorizedException("Authorization required to access this resource.");
        }

        if ("NO_AUTH".equals(systemConfig.getAuthenticationProviderId())) {
            return;
        }

        Method method = resourceInfo.getResourceMethod();
        Class<?> resourceClass = resourceInfo.getResourceClass();

        if (method.isAnnotationPresent(DenyAll.class)) {
            throw new ForbiddenException("Access denied.");
        }
        if (method.isAnnotationPresent(PermitAll.class)) {
            return;
        }

        RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);
        if (rolesAllowed != null) {
            checkRoles(rolesAllowed);
            return;
        }

        if (resourceClass.isAnnotationPresent(DenyAll.class)) {
            throw new ForbiddenException("Access denied.");
        }
        if (resourceClass.isAnnotationPresent(PermitAll.class)) {
            return;
        }

        rolesAllowed = resourceClass.getAnnotation(RolesAllowed.class);
        if (rolesAllowed != null) {
            checkRoles(rolesAllowed);
            return;
        }
    }

    private void checkRoles(RolesAllowed rolesAllowed) {
        for (String role : rolesAllowed.value()) {
            if (userService.hasLoggedInUserRole(role)) {
                return;
            }
        }
        throw new ForbiddenException(
                "Insufficient privileges: requires one of " + Arrays.toString(rolesAllowed.value()));
    }
}
