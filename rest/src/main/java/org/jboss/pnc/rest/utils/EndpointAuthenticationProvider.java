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

package org.jboss.pnc.rest.utils;

import org.apache.commons.lang3.StringUtils;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.Datastore;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 8/12/16
 * Time: 1:28 PM
 */
@ApplicationScoped
public class EndpointAuthenticationProvider {


    private Datastore datastore;

    @Deprecated
    public EndpointAuthenticationProvider() {
    }

    @Inject
    public EndpointAuthenticationProvider(Datastore datastore) {
        this.datastore = datastore;
    }

    public User getCurrentUser(HttpServletRequest httpServletRequest) {
        AuthenticationProvider authProvider = new AuthenticationProvider(httpServletRequest);
        String loggedUser = authProvider.getUserName();
        User currentUser = null;
        if(StringUtils.isNotEmpty(loggedUser)) {
            currentUser = datastore.retrieveUserByUsername(loggedUser);
            if(currentUser != null) {
                currentUser.setLoginToken(authProvider.getTokenString());
            }
        }
        return currentUser;
    }
}
