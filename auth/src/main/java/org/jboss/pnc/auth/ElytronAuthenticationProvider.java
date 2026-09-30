/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth;

import javax.enterprise.context.Dependent;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
@AuthProvider
public class ElytronAuthenticationProvider implements AuthenticationProvider {

    public static final String ID = "Elytron";

    public final static Logger log = LoggerFactory.getLogger(ElytronAuthenticationProvider.class);

    @Override
    public LoggedInUser getLoggedInUser(HttpServletRequest httpServletRequest) {
        return new ElytronLoggedInUser(httpServletRequest);
    }

    @Override
    public String getId() {
        return ID;
    }
}