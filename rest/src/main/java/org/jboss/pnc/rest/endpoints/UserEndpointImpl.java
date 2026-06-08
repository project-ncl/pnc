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
        // Validate redirect URL to prevent open redirect attacks
        if (redirectPath == null || redirectPath.trim().isEmpty()) {
            return redirectToHomePage();
        } else if (redirectPath.startsWith("/")) {
            redirectPath = redirectPath.substring(1);
        }

        String[] urlToRedirect = redirectPath.split("/", 3);

        if (urlToRedirect.length >= 2) {
            String scheme = urlToRedirect[0];
            String serverName = urlToRedirect[1];

            // Validate scheme - only allow http/https
            if (!scheme.equals("http") && !scheme.equals("https")) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid redirect scheme").build();
            }

            // Validate that the redirect is to same origin or localhost
            if (!isAllowedRedirectHost(serverName)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Redirect to external domain not allowed")
                        .build();
            }

            String endPart = urlToRedirect.length == 3 ? urlToRedirect[2] : "/";
            String absoluteUrl = scheme + "://" + serverName + "/" + endPart;
            return Response.status(Response.Status.FOUND).location(URI.create(absoluteUrl)).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity("Redirect path contains an invalid url").build();
        }
    }

    private Response redirectToHomePage() {
        String redirectPath;
        String absoluteUrl;

        redirectPath = "/";
        // Build an absolute URI to avoid relative path resolution issues
        String scheme = servletRequest.getScheme();
        String serverName = servletRequest.getServerName();
        int serverPort = servletRequest.getServerPort();

        if ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443)) {
            absoluteUrl = scheme + "://" + serverName + redirectPath;
        } else {
            absoluteUrl = scheme + "://" + serverName + ":" + serverPort + redirectPath;
        }

        return Response.status(Response.Status.FOUND).location(URI.create(absoluteUrl)).build();
    }

    @Override
    public Response logoutAndRedirect(String redirectPath) {
        // Invalidate the session
        if (servletRequest.getSession(false) != null) {
            servletRequest.getSession().invalidate();
        }

        // Validate redirect URL to prevent open redirect attacks
        if (redirectPath == null || redirectPath.trim().isEmpty()) {
            return redirectToHomePage();
        } else if (redirectPath.startsWith("/")) {
            redirectPath = redirectPath.substring(1);
        }

        String[] urlToRedirect = redirectPath.split("/", 3);

        if (urlToRedirect.length >= 2) {
            String scheme = urlToRedirect[0];
            String serverName = urlToRedirect[1];

            // Validate scheme - only allow http/https
            if (!scheme.equals("http") && !scheme.equals("https")) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid redirect scheme").build();
            }

            // Validate that the redirect is to same origin or localhost
            if (!isAllowedRedirectHost(serverName)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Redirect to external domain not allowed")
                        .build();
            }

            String endPart = urlToRedirect.length == 3 ? urlToRedirect[2] : "/";
            String absoluteUrl = scheme + "://" + serverName + "/" + endPart;
            return Response.status(Response.Status.FOUND).location(URI.create(absoluteUrl)).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity("Redirect path contains an invalid url").build();
        }
    }

    private boolean isAllowedRedirectHost(String targetHost) {
        String requestServerName = servletRequest.getServerName();

        // Remove port from target host if present (e.g., "localhost:3000" -> "localhost")
        String targetHostWithoutPort = targetHost.split(":")[0];

        // Allow same origin
        if (targetHostWithoutPort.equals(requestServerName)) {
            return true;
        }

        // Allow localhost and 127.0.0.1
        if (targetHostWithoutPort.equals("localhost") || targetHostWithoutPort.equals("127.0.0.1")) {
            return true;
        }

        return false;
    }

    @Override
    public Page<Build> getBuilds(String id, PageParameters page, BuildsFilterParameters filter) {
        BuildPageInfo pageInfo = BuildEndpointImpl.toBuildPageInfo(page, filter);
        return buildProvider.getBuildsForUser(pageInfo, id);
    }
}
