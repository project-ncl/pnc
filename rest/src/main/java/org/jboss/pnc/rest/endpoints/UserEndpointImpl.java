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

import org.jboss.pnc.auth.OidcDiscoveryService;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.KeycloakClientConfig;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.User;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.BuildPageInfo;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.facade.providers.api.UserProvider;
import org.jboss.pnc.rest.api.endpoints.UserEndpoint;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class UserEndpointImpl implements UserEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(UserEndpointImpl.class);

    @Inject
    private UserProvider userProvider;

    @Inject
    private BuildProvider buildProvider;

    @Inject
    private SystemConfig systemConfig;

    @Inject
    private OidcDiscoveryService oidcDiscoveryService;

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
        // Get the ID token before invalidating the session
        String idToken = getIdTokenFromSession();

        // Invalidate the session
        if (servletRequest.getSession(false) != null) {
            servletRequest.getSession().invalidate();
        }

        // Build the post-logout redirect URL
        String postLogoutRedirectUrl = buildPostLogoutRedirectUrl(redirectPath);
        if (postLogoutRedirectUrl == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid redirect path").build();
        }

        // Redirect to SSO logout endpoint (discovered via OIDC)
        String ssoLogoutUrl = buildSsoLogoutUrl(postLogoutRedirectUrl, idToken);
        if (ssoLogoutUrl == null) {
            // If SSO config is not available or discovery fails, just do local logout
            logger.warn("SSO logout endpoint not available, performing local logout only");
            return Response.status(Response.Status.FOUND).location(URI.create(postLogoutRedirectUrl)).build();
        }

        return Response.status(Response.Status.FOUND).location(URI.create(ssoLogoutUrl)).build();
    }

    /**
     * Retrieves the ID token from the current session. The ID token is used for SSO logout as id_token_hint.
     *
     * @return The ID token string, or null if not available
     */
    private String getIdTokenFromSession() {
        try {
            // Try Elytron OIDC first (newer approach)
            org.wildfly.security.http.oidc.OidcSecurityContext oidcSecurityContext = (org.wildfly.security.http.oidc.OidcSecurityContext) servletRequest
                    .getAttribute(org.wildfly.security.http.oidc.OidcSecurityContext.class.getName());

            if (oidcSecurityContext != null) {
                return oidcSecurityContext.getIDTokenString();
            }

            // Fallback to Keycloak (legacy approach)
            org.keycloak.KeycloakSecurityContext keycloakSecurityContext = (org.keycloak.KeycloakSecurityContext) servletRequest
                    .getAttribute(org.keycloak.KeycloakSecurityContext.class.getName());

            if (keycloakSecurityContext != null) {
                return keycloakSecurityContext.getIdTokenString();
            }
        } catch (NoClassDefFoundError e) {
            logger.debug("OIDC/Keycloak libraries not available", e);
        } catch (Exception e) {
            logger.warn("Failed to retrieve ID token from session", e);
        }

        return null;
    }

    /**
     * Builds the URL to redirect to after SSO logout completes
     */
    private String buildPostLogoutRedirectUrl(String redirectPath) {
        // Validate redirect URL to prevent open redirect attacks
        if (redirectPath == null || redirectPath.trim().isEmpty()) {
            return buildAbsoluteUrl("/");
        }

        if (redirectPath.startsWith("/")) {
            redirectPath = redirectPath.substring(1);
        }

        String[] urlToRedirect = redirectPath.split("/", 3);

        if (urlToRedirect.length >= 2) {
            String scheme = urlToRedirect[0];
            String serverName = urlToRedirect[1];

            // Validate scheme - only allow http/https
            if (!scheme.equals("http") && !scheme.equals("https")) {
                return null;
            }

            // Validate that the redirect is to same origin or localhost
            if (!isAllowedRedirectHost(serverName)) {
                return null;
            }

            String endPart = urlToRedirect.length == 3 ? urlToRedirect[2] : "/";
            return scheme + "://" + serverName + "/" + endPart;
        }

        return null;
    }

    /**
     * Builds the SSO logout URL using OIDC Discovery. This method works with any OIDC-compliant provider (Keycloak, IBM
     * Verify, Auth0, Okta, etc.) by discovering the end_session_endpoint from the provider's metadata.
     *
     * @param postLogoutRedirectUri The URI to redirect to after SSO logout completes
     * @param idToken The ID token from the current session (may be null)
     * @return The SSO logout URL, or null if discovery fails
     */
    private String buildSsoLogoutUrl(String postLogoutRedirectUri, String idToken) {
        try {
            // Get the OIDC issuer URL from configuration
            String issuerUrl = getOidcIssuerUrl();
            if (issuerUrl == null) {
                logger.warn("OIDC issuer URL not configured");
                return null;
            }

            // Discover the SSO provider's metadata
            OidcDiscoveryService.OidcProviderMetadata metadata = oidcDiscoveryService.discover(issuerUrl);
            if (metadata == null) {
                logger.warn("Failed to discover OIDC metadata for issuer: {}", issuerUrl);
                return null;
            }

            // Get the end_session_endpoint (logout endpoint)
            String endSessionEndpoint = metadata.getEndSessionEndpoint();
            if (endSessionEndpoint == null || endSessionEndpoint.trim().isEmpty()) {
                logger.warn("SSO provider does not advertise an end_session_endpoint");
                return null;
            }

            // Build the logout URL with required parameters
            StringBuilder logoutUrl = new StringBuilder(endSessionEndpoint);
            logoutUrl.append("?");

            // Add id_token_hint if available (required by most OIDC providers)
            if (idToken != null && !idToken.trim().isEmpty()) {
                String encodedIdToken = URLEncoder.encode(idToken, StandardCharsets.UTF_8.toString());
                logoutUrl.append("id_token_hint=").append(encodedIdToken).append("&");
            }

            // Add post_logout_redirect_uri
            String encodedRedirectUri = URLEncoder.encode(postLogoutRedirectUri, StandardCharsets.UTF_8.toString());
            logoutUrl.append("post_logout_redirect_uri=").append(encodedRedirectUri);

            return logoutUrl.toString();

        } catch (UnsupportedEncodingException e) {
            logger.error("Failed to encode logout URL parameters", e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error building SSO logout URL", e);
            return null;
        }
    }

    /**
     * Gets the OIDC issuer URL from environment variable or configuration. Checks in order: 1. OIDC_PROVIDER_URL
     * environment variable (recommended) 2. KeycloakClientConfig from system configuration (fallback for backward
     * compatibility)
     *
     * @return The OIDC issuer URL, or null if not configured
     */
    private String getOidcIssuerUrl() {
        // First, check for environment variable
        String issuerUrl = System.getenv("OIDC_PROVIDER_URL");
        if (issuerUrl != null && !issuerUrl.trim().isEmpty()) {
            logger.debug("Using OIDC issuer URL from environment variable: {}", issuerUrl);
            // Remove trailing slash if present
            return issuerUrl.endsWith("/") ? issuerUrl.substring(0, issuerUrl.length() - 1) : issuerUrl;
        }

        // Fallback to configuration file (for backward compatibility with Keycloak setup)
        KeycloakClientConfig keycloakConfig = systemConfig.getKeycloakServiceAccountConfig();
        if (keycloakConfig == null) {
            logger.debug("SSO configuration is null and OIDC_PROVIDER_URL not set");
            return null;
        }

        String authServerUrl = keycloakConfig.getAuthServerUrl();
        String realm = keycloakConfig.getRealm();

        if (authServerUrl == null) {
            logger.warn("SSO auth server URL is null and OIDC_PROVIDER_URL not set");
            return null;
        }

        // Remove trailing slash from auth server URL if present
        if (authServerUrl.endsWith("/")) {
            authServerUrl = authServerUrl.substring(0, authServerUrl.length() - 1);
        }

        // For Keycloak, the issuer is {auth-server-url}/realms/{realm}
        // For other providers, the auth-server-url might already be the full issuer
        if (realm != null && !realm.trim().isEmpty()) {
            issuerUrl = authServerUrl + "/realms/" + realm;
        } else {
            // If no realm specified, assume auth-server-url is the issuer
            issuerUrl = authServerUrl;
        }

        logger.debug("Using OIDC issuer URL from configuration: {}", issuerUrl);
        return issuerUrl;
    }

    /**
     * Builds an absolute URL from a relative path
     */
    private String buildAbsoluteUrl(String path) {
        String scheme = servletRequest.getScheme();
        String serverName = servletRequest.getServerName();
        int serverPort = servletRequest.getServerPort();

        if ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443)) {
            return scheme + "://" + serverName + path;
        } else {
            return scheme + "://" + serverName + ":" + serverPort + path;
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
