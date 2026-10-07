/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class NoAuthLoggedInUser implements LoggedInUser {

    private HttpServletRequest httpServletRequest;

    public NoAuthLoggedInUser() {
    }

    @Override
    public String getEmail() {
        return "demo-user@pnc.com";
    }

    @Override
    public String getUserName() {
        return "demo-user";
    }

    @Override
    public String getFirstName() {
        return "Demo First Name";
    }

    @Override
    public String getLastName() {
        return "Demo Last Name";
    }

    @Override
    public Set<String> getRole() {
        return new HashSet<>();
    }

    @Override
    public boolean isUserInRole(String role) {
        return role.contains(role);
    }

    @Override
    public String getTokenString() {
        return "sso-not-enabled-no-token";
    }

}
