/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth;

import javax.enterprise.context.Dependent;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides access to authenticated user info. In case no authentication is configured or there are problems
 * with authentication the default demo-user is returned instead
 *
 * @author pslegr
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 *
 */
@Dependent
@AuthProvider
public class KeycloakAuthenticationProvider implements AuthenticationProvider {

    public static final String ID = "Keycloak";

    public final static Logger log = LoggerFactory.getLogger(KeycloakAuthenticationProvider.class);

    @Override
    public LoggedInUser getLoggedInUser(HttpServletRequest httpServletRequest) {
        return new KeycloakLoggedInUser(httpServletRequest);
    }

    @Override
    public String getId() {
        return ID;
    }

}
