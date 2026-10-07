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
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Dependent
@AuthProvider
public class JaasAuthenticationProvider implements AuthenticationProvider {

    public static final String ID = "JAAS";

    public final static Logger log = LoggerFactory.getLogger(JaasAuthenticationProvider.class);

    @Override
    public LoggedInUser getLoggedInUser(HttpServletRequest httpServletRequest) {
        return new JAASLoggedInUser(httpServletRequest);
    }

    @Override
    public String getId() {
        return ID;
    }

}
