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
package org.jboss.pnc.rest.endpoints;

import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.AuthenticationProviderFactory;
import org.jboss.pnc.auth.LoggedInUser;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.User;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.facade.providers.api.UserProvider;
import org.jboss.pnc.rest.api.endpoints.UserEndpoint;
import org.jboss.pnc.rest.api.parameters.PageParameters;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

@Stateless
public class UserEndpointImpl
        extends AbstractEndpoint<User, User>
        implements UserEndpoint {

    @Context
    private HttpServletRequest httpServletRequest;

    private UserProvider userProvider;
    private AuthenticationProvider authenticationProvider;
    private BuildProvider buildProvider;

    public UserEndpointImpl() {
        super(User.class);
    }

    @Inject
    public UserEndpointImpl(UserProvider userProvider,
                            BuildProvider buildProvider,
                            AuthenticationProviderFactory authenticationProviderFactory) {

        super(userProvider, User.class);

        this.userProvider = userProvider;
        this.buildProvider = buildProvider;
        this.authenticationProvider = authenticationProviderFactory.getProvider();
    }

    @Override
    public User getCurrentUser() {

        LoggedInUser loginInUser = authenticationProvider.getLoggedInUser(httpServletRequest);
        return userProvider.getOrCreateNewUser(loginInUser.getUserName());
    }


    @Override
    public Page<Build> getBuilds(int id, PageParameters pageParameters) {

        return buildProvider.getBuildsForUser(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ(),
                id);
    }
}
