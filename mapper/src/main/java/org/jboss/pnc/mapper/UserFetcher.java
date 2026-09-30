/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper;

import static org.jboss.pnc.spi.datastore.predicates.UserPredicates.withUserName;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.jboss.pnc.mapper.api.ByUsername;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;

@ApplicationScoped
@Transactional
public class UserFetcher {

    private UserRepository userRepository;

    // CDI
    public UserFetcher() {
    }

    @Inject
    public UserFetcher(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ByUsername
    public User toUserReference(String username) {
        return userRepository.queryByPredicates(withUserName(username));
    }
}
