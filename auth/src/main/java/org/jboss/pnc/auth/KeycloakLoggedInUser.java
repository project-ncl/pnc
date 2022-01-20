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

import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class KeycloakLoggedInUser implements LoggedInUser {

    private final static Logger log = LoggerFactory.getLogger(KeycloakLoggedInUser.class);

    public final static String MSG = "Authentication could not be enabled";

    private AccessToken auth;
    private String tokenString;

    public KeycloakLoggedInUser(HttpServletRequest httpServletRequest) {
        if (httpServletRequest == null) {
            throw new NullPointerException();
        }
        try {
            KeycloakSecurityContext keycloakSecurityContext = (KeycloakSecurityContext) httpServletRequest
                    .getAttribute(KeycloakSecurityContext.class.getName());
            if (keycloakSecurityContext == null) {
                handleAuthenticationProblem("KeycloakSecurityContext not available in the HttpServletRequest.");
            } else {
                this.auth = keycloakSecurityContext.getToken();
                this.tokenString = keycloakSecurityContext.getTokenString();
            }
        } catch (NoClassDefFoundError ncdfe) {
            handleAuthenticationProblem(ncdfe.getMessage(), ncdfe);
        }
    }

    @Override
    public String getEmail() {
        return auth.getEmail();
    }

    @Override
    public String getUserName() {
        return this.auth.getPreferredUsername();
    }

    @Override
    public String getFirstName() {
        return this.auth.getGivenName();
    }

    @Override
    public String getLastName() {
        return this.auth.getFamilyName();
    }

    @Override
    public Set<String> getRole() {
        return this.auth.getRealmAccess().getRoles();
    }

    @Override
    public boolean isUserInRole(String role) {
        return this.auth.getRealmAccess().isUserInRole(role);
    }

    @Override
    public String getTokenString() {
        return tokenString;
    }

    @Override
    public String toString() {
        return "KeycloakLoggedInUser [auth=" + auth + ", getEmail()=" + getEmail() + ", getUserName()=" + getUserName()
                + ", getFirstName()=" + getFirstName() + ", getLastName()=" + getLastName() + ", getRole()=" + getRole()
                + ", getTokenString()=***]";
    }

    private void handleAuthenticationProblem(String warning) {
        handleAuthenticationProblem(warning, null);
    }

    private void handleAuthenticationProblem(String warning, Throwable cause) {
        log.warn(MSG + ": " + warning, cause);
        throw new AuthenticationException(MSG + ": " + warning, cause);
    }

}
