/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.facade.util;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.LoggedInUser;
import org.jboss.pnc.model.User;
import static org.jboss.pnc.spi.datastore.predicates.UserPredicates.withUserName;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbrazdil
 */
@RequestScoped
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Inject
    private AuthenticationProvider authenticationProvider;

    @Inject
    private HttpServletRequest httpServletRequest;

    @Inject
    private UserRepository repository;

    public String currentUserToken() {
        logger.trace("Getting current user token using authenticationProvider: {}.", authenticationProvider.getId());
        LoggedInUser currentUser = authenticationProvider.getLoggedInUser(httpServletRequest);
        logger.trace("LoggedInUser: {}.", currentUser);
        return currentUser.getTokenString();
    }

    public User currentUser() {
        logger.trace("Getting current user using authenticationProvider: {}.", authenticationProvider.getId());
        LoggedInUser currentUser = authenticationProvider.getLoggedInUser(httpServletRequest);
        logger.trace("LoggedInUser: {}.", currentUser);
        String username = currentUser.getUserName();

        User user = null;
        if (StringUtils.isNotEmpty(username)) {
            user = repository.queryByPredicates(withUserName(username));
            if (user != null) {
                user.setLoginToken(currentUser.getTokenString());
            } else {
                throw new IllegalStateException("User not in database: Login in UI to create user.");
            }
        }
        logger.trace("Returning user: {}.", user);
        return user;
    }
}
