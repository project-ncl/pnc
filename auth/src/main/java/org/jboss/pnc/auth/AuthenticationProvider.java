/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

import org.jboss.logging.Logger;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * This class provides access to authenticated user info. In case no authentication
 * is configured or there are problems with authentication the default demo-user is
 * returned instead
 *
 * @author pslegr
 *
 */
public class AuthenticationProvider {
    public final static Logger log = Logger.getLogger(AuthenticationProvider.class);
    public final static String MSG = "Authentication could not be enabled";

    private static boolean enabled;

    static {
        InputStream propertiesStream = AuthenticationProvider.class.getResourceAsStream("/authentication.properties");
        if (propertiesStream == null) {
            throw new AuthenticationException("authentication.properties not found");
        }

        Properties properties = new Properties();
        try {
            properties.load(propertiesStream);
            String enabledProperty = properties.getProperty("authentication.enabled");
            enabled = Boolean.valueOf(enabledProperty);
        } catch (IOException e) {
            throw new AuthenticationException("Error processing authentication.properties", e);
        }
    }


    private AccessToken auth;
    private String tokenString;

    public AuthenticationProvider(HttpServletRequest req) throws AuthenticationException {
        try {
            KeycloakSecurityContext keycloakSecurityContext = (KeycloakSecurityContext) req.getAttribute(KeycloakSecurityContext.class.getName());
            if(keycloakSecurityContext == null) {
                handleAuthenticationProblem("KeycloakSecurityContext not available in the HttpServletRequest.");
            } else {
                this.auth = keycloakSecurityContext.getToken();
                this.tokenString = keycloakSecurityContext.getTokenString();
            }
        } catch (NoClassDefFoundError ncdfe) {
            handleAuthenticationProblem(ncdfe.getMessage(), ncdfe);
        }
    }

    private void handleAuthenticationProblem(String warning) {
        handleAuthenticationProblem(warning, null);
    }

    private void handleAuthenticationProblem(String warning, Throwable cause) {
        log.warn(MSG + ": " + warning, cause);
        if (enabled) {
            throw new AuthenticationException(MSG + warning, cause);
        } else {
            log.warn("using " + DemoUser.username + " instead");
        }
    }

    public String getEmail() {
        if(!enabled) {
            return DemoUser.email;
        }
        return auth.getEmail();
    }

    public String getUserName() {
        if(!enabled) {
            return DemoUser.username;
        }
        return this.auth.getPreferredUsername();
    }

    public String getFirstName() {
        if(!enabled) {
            return DemoUser.firstname;
        }
        return this.auth.getGivenName();
    }

    public String getLastName() {
        if(!enabled) {
            return DemoUser.lastname;
        }
        return this.auth.getFamilyName();
    }


    public Set<String> getRole() {
        if(!enabled) {
            return DemoUser.roles;
        }
        return this.auth.getRealmAccess().getRoles();
    }

    public boolean isUserInRole(String role) {
        if(!enabled) {
            return DemoUser.hasRole(role);
        }
        return this.auth.getRealmAccess().isUserInRole(role);
    }

    public AccessToken getAccessToken() {
        return auth;
    }

    public String getTokenString() {
        if(!enabled) {
            return DemoUser.token;
        }
        return tokenString;
    }

    @Override
    public String toString() {
        return "AuthenticationProvider [auth=" + auth + ", getEmail()=" + getEmail() + ", getUserName()="
                + getUserName() + ", getFirstName()=" + getFirstName() + ", getLastName()=" + getLastName() + ", getRole()="
                + getRole() + ", getAccessToken()=" + getAccessToken() + ", getTokenString()=" + getTokenString() + "]";
    }

    private final static class DemoUser {
        static String token = "sso-not-enabled-no-token";
        static String username = "demo-user";
        static String firstname = "Demo First Name";
        static String lastname = "Demo Last Name";
        static String email = "demo-user@pnc.com";
        static Set<String> roles = new HashSet<>();
        static {
            roles.add("user");
        }
        public final static boolean hasRole(String role) {
            return role.contains(role);
        }
    }
}
