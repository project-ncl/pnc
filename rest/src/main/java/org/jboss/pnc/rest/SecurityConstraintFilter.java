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
import javax.inject.Inject;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Post-matching JAX-RS filter that enforces authentication (401). Role-based authorization (403) is handled by
 * RESTEasy's built-in SecurityInterceptor via the {@code resteasy.role.based.security} context parameter in web.xml.
 */
@Provider
@Priority(10)
public class SecurityConstraintFilter implements ContainerRequestFilter {

    @Inject
    UserService userService;

    @Inject
    SystemConfig systemConfig;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if ("NO_AUTH".equals(systemConfig.getAuthenticationProviderId())) {
            return;
        }

        if (!userService.isUserLoggedIn()) {
            throw new NotAuthorizedException("Authorization required to access this resource.");
        }
    }
}