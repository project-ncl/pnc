/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.util;

import static org.jboss.pnc.spi.datastore.predicates.UserPredicates.withUserName;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.LoggedInUser;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.model.User;
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

    public boolean isUserLoggedIn() {
        return httpServletRequest.getUserPrincipal() != null;
    }

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
        user.setRoles(currentUser.getRole());
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
