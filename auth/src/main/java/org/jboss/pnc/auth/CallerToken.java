/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.wildfly.security.http.oidc.OidcSecurityContext;

/**
 * The OIDC access token a caller authenticated with, to call another service on the caller's behalf, so that the
 * other service checks the caller's own roles.
 * <p>
 * Kept apart from {@link LoggedInUser#getTokenString()}, which ends up in {@code User} objects that are logged.
 */
public final class CallerToken {

    private CallerToken() {
    }

    /**
     * @param request the current request
     * @return the access token Elytron validated for this request; empty for callers that did not authenticate with
     *         an OIDC bearer token (LDAP, anonymous) or when OIDC is not available
     */
    public static Optional<String> of(HttpServletRequest request) {
        if (request == null) {
            return Optional.empty();
        }
        try {
            Object context = request.getAttribute(OidcSecurityContext.class.getName());
            if (context instanceof OidcSecurityContext) {
                String token = ((OidcSecurityContext) context).getTokenString();
                if (token != null && !token.isBlank()) {
                    return Optional.of(token);
                }
            }
        } catch (NoClassDefFoundError e) {
            // OIDC not deployed: no caller token
        }
        return Optional.empty();
    }
}
