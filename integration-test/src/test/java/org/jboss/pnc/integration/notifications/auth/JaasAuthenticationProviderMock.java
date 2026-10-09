/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integration.notifications.auth;

import javax.enterprise.context.Dependent;
import javax.servlet.http.HttpServletRequest;

import org.jboss.pnc.auth.AuthProvider;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.LoggedInUser;
import org.jboss.pnc.auth.NoAuthLoggedInUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentication provider which acts as a {@link org.jboss.pnc.auth.NoAuthAuthenticationProvider}, however, it has got
 * the ID of the {@link org.jboss.pnc.auth.JaasAuthenticationProvider}.
 */
@Dependent
@AuthProvider
public class JaasAuthenticationProviderMock implements AuthenticationProvider {

    public static final String ID = "JAAS";

    public final static Logger log = LoggerFactory.getLogger(org.jboss.pnc.auth.NoAuthAuthenticationProvider.class);

    private HttpServletRequest httpServletRequest = null;

    public JaasAuthenticationProviderMock() {
    }

    @Override
    public LoggedInUser getLoggedInUser(HttpServletRequest httpServletRequest) {
        return new NoAuthLoggedInUser();
    }

    @Override
    public String getId() {
        return ID;
    }

}
