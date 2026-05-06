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
package org.jboss.pnc.rest.endpoints;

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.User;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.BuildPageInfo;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.facade.providers.api.UserProvider;
import org.jboss.pnc.rest.api.endpoints.UserEndpoint;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URI;

@ApplicationScoped
public class UserEndpointImpl implements UserEndpoint {

    @Inject
    private UserProvider userProvider;

    @Inject
    private BuildProvider buildProvider;

    @Context
    private HttpServletRequest servletRequest;

    @Override
    public User getCurrentUser() {
        return userProvider.getCurrentUser();
    }

    @Override
    public Response loginAndRedirect(String redirectPath) {
        // Ensure path starts with / to make it an absolute path
        if (redirectPath == null || redirectPath.trim().isEmpty()) {
            redirectPath = "/";
        } else if (!redirectPath.startsWith("/")) {
            redirectPath = "/" + redirectPath;
        }

        // Build an absolute URI to avoid relative path resolution issues
        String scheme = servletRequest.getScheme();
        String serverName = servletRequest.getServerName();
        int serverPort = servletRequest.getServerPort();

        String absoluteUrl;
        if ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443)) {
            absoluteUrl = scheme + "://" + serverName + redirectPath;
        } else {
            absoluteUrl = scheme + "://" + serverName + ":" + serverPort + redirectPath;
        }

        return Response.status(Response.Status.FOUND).location(URI.create(absoluteUrl)).build();
    }

    @Override
    public Page<Build> getBuilds(String id, PageParameters page, BuildsFilterParameters filter) {
        BuildPageInfo pageInfo = BuildEndpointImpl.toBuildPageInfo(page, filter);
        return buildProvider.getBuildsForUser(pageInfo, id);
    }
}
