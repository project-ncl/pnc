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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.SecurityContext;
import java.util.Set;

public class ElytronLoggedInUser implements LoggedInUser {

    private final static Logger log = LoggerFactory.getLogger(ElytronLoggedInUser.class);

    public final static String MSG = "Authentication could not be enabled";

    private SecurityContext securityContext;

    public ElytronLoggedInUser(HttpServletRequest httpServletRequest) {
        if (httpServletRequest == null) {
            throw new NullPointerException();
        }
        try {
            securityContext = (SecurityContext) httpServletRequest.getAttribute(SecurityContext.class.getName());
            if (securityContext == null) {
                handleAuthenticationProblem("SecurityContext not available in the HttpServletRequest.", null);
            }
        } catch (NoClassDefFoundError ncdfe) {
            handleAuthenticationProblem(ncdfe.getMessage(), ncdfe);
        }
    }

    @Override
    public String getEmail() {
        return securityContext.getUserPrincipal().getName();
    }

    @Override
    public String getUserName() {
        return securityContext.getUserPrincipal().getName();
    }

    @Override
    public String getFirstName() {
        return securityContext.getUserPrincipal().getName();
    }

    @Override
    public String getLastName() {
        return securityContext.getUserPrincipal().getName();
    }

    @Override
    public Set<String> getRole() {
        return Set.of();
    }

    @Override
    public boolean isUserInRole(String role) {
        return securityContext.isUserInRole(role);
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
