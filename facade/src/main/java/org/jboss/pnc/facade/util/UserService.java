/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.LoggedInUser;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import static org.jboss.pnc.spi.datastore.predicates.UserPredicates.withUserName;

/**
 *
 * @author jbrazdil
 */
@RequestScoped
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_SYSTEM_USER = "system-user";

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

    public String currentUsername() {
        logger.trace("Getting current user token using authenticationProvider: {}.", authenticationProvider.getId());
        LoggedInUser currentUser = authenticationProvider.getLoggedInUser(httpServletRequest);
        logger.trace("LoggedInUser: {}.", currentUser);
        return currentUser.getUserName();
    }

    public User currentUser() {
        logger.trace("Getting current user using authenticationProvider: {}.", authenticationProvider.getId());
        LoggedInUser currentUser = authenticationProvider.getLoggedInUser(httpServletRequest);
        logger.trace("LoggedInUser: {}.", currentUser);
        String username = currentUser.getUserName();

        if (StringUtils.isEmpty(username)) {
            return null;
        }

        User user = getOrCreate(currentUser, username);
        user.setLoginToken(currentUser.getTokenString());
        logger.trace("Returning user: {}.", user);
        return user;
    }

    public boolean hasLoggedInUserRole(String role) {
        logger.trace("Getting current user using authenticationProvider: {}.", authenticationProvider.getId());
        LoggedInUser currentUser = authenticationProvider.getLoggedInUser(httpServletRequest);
        logger.trace("LoggedInUser: {}.", currentUser);
        return currentUser.isUserInRole(role);
    }

    private User getOrCreate(LoggedInUser loggedInUser, String username) {
        User user = repository.queryByPredicates(withUserName(username));
        if (user == null) {
            logger.info("Adding new user to the local database: {}.", loggedInUser);
            String syncObject = username.intern();
            synchronized (syncObject) {
                user = repository.queryByPredicates(withUserName(username));
                if (user == null) {
                    user = User.Builder.newBuilder()
                            .username(username)
                            .firstName(loggedInUser.getFirstName())
                            .lastName(loggedInUser.getLastName())
                            .email(loggedInUser.getEmail())
                            .build();
                    repository.save(user);
                }
            }
        }
        return user;
    }
}
