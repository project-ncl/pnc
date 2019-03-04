/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

package org.jboss.pnc.rest.utils;

import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.AuthenticationProviderFactory;
import org.jboss.pnc.auth.LoggedInUser;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 8/12/16
 * Time: 1:28 PM
 */
@ApplicationScoped
public class EndpointAuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(EndpointAuthenticationProvider.class);

    private Datastore datastore;

    private AuthenticationProvider authenticationProvider;

    @Deprecated
    public EndpointAuthenticationProvider() {
    }

    @Inject
    public EndpointAuthenticationProvider(Datastore datastore, AuthenticationProviderFactory authenticationProviderFactory) {
        this.datastore = datastore;
        this.authenticationProvider = authenticationProviderFactory.getProvider();
    }

    public User getCurrentUser(HttpServletRequest httpServletRequest) {
        logger.trace("Getting current user using authenticationProvider: {}.", authenticationProvider.getId());
        LoggedInUser loginInUser = authenticationProvider.getLoggedInUser(httpServletRequest);
        logger.trace("LoggedInUser: {}.", loginInUser);
        String loggedUser = loginInUser.getUserName();
        User currentUser = null;
        if(StringUtils.isNotEmpty(loggedUser)) {
            currentUser = datastore.retrieveUserByUsername(loggedUser);
            if(currentUser != null) {
                currentUser.setLoginToken(loginInUser.getTokenString());
            }
        }
        logger.trace("Returning user: {}.", currentUser);
        return currentUser;
    }
}
