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
package org.jboss.pnc.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.security.http.oidc.AccessToken;
import org.wildfly.security.http.oidc.OidcSecurityContext;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.SecurityContext;
import java.util.HashSet;
import java.util.Set;

public class ElytronLoggedInUser implements LoggedInUser {

    private final static Logger log = LoggerFactory.getLogger(ElytronLoggedInUser.class);

    public final static String MSG = "Authentication could not be enabled";

    private AccessToken accessToken;

    public ElytronLoggedInUser(HttpServletRequest httpServletRequest) {
        try {
            OidcSecurityContext oidcSecurityContext = (OidcSecurityContext) httpServletRequest
                    .getAttribute(OidcSecurityContext.class.getName());
            if (oidcSecurityContext == null) {
                handleAuthenticationProblem("OidcSecurityContext not available in the HttpServletRequest.", null);
            } else {
                this.accessToken = oidcSecurityContext.getToken();
            }
        } catch (NoClassDefFoundError ncdfe) {
            handleAuthenticationProblem(ncdfe.getMessage(), ncdfe);
        }
    }

    @Override
    public String getEmail() {
        return accessToken.getClaimValueAsString("email");
    }

    @Override
    public String getUserName() {
        return accessToken.getClaimValueAsString("preferred_username");
    }

    @Override
    public String getFirstName() {
        return accessToken.getClaimValueAsString("firstName");
    }

    @Override
    public String getLastName() {
        return accessToken.getClaimValueAsString("lastName");
    }

    @Override
    public Set<String> getRole() {
        return new HashSet<>(accessToken.getRealmAccessClaim().getRoles());
    }

    @Override
    public boolean isUserInRole(String role) {
        return accessToken.getRealmAccessClaim().getRoles().contains(role);
    }

    @Override
    public String getTokenString() {
        return "";
    }

    private void handleAuthenticationProblem(String warning, Throwable cause) {
        log.warn(MSG + ": " + warning, cause);
        throw new AuthenticationException(MSG + ": " + warning, cause);
    }
}
