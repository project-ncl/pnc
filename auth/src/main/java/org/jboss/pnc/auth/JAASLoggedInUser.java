/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth;

import java.util.Collections;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class JAASLoggedInUser implements LoggedInUser {

    public final static Logger log = LoggerFactory.getLogger(JAASLoggedInUser.class);

    private HttpServletRequest httpServletRequest;

    public JAASLoggedInUser(HttpServletRequest httpServletRequest) {
        if (httpServletRequest == null) {
            throw new NullPointerException();
        }
        this.httpServletRequest = httpServletRequest;
        log.debug("Instantiated new object for username: {}.", getUserName());
    }

    @Override
    public String getEmail() {
        return getUserName() + "@" + "not.available";
    }

    @Override
    public String getUserName() {
        if (httpServletRequest.getUserPrincipal() == null) {
            return null;
        }
        return httpServletRequest.getUserPrincipal().getName();
    }

    @Override
    public String getFirstName() {
        return "First Name N/A (" + getUserName() + ")";
    }

    @Override
    public String getLastName() {
        return "Last Name N/A (" + getUserName() + ")";
    }

    @Override
    public Set<String> getRole() {
        return Collections.emptySet();
    }

    @Override
    public boolean isUserInRole(String role) {
        return httpServletRequest.isUserInRole(role);
    }

    @Override
    public String getTokenString() {
        return "--NO-TOKEN-AVAILABLE--";
    }

}
