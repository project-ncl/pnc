/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.dto.User;
import org.jboss.pnc.facade.providers.api.UserProvider;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.mapper.api.UserMapper;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PermitAll
@Stateless
public class UserProviderImpl extends AbstractProvider<Integer, org.jboss.pnc.model.User, User, User>
        implements UserProvider {

    private static final Logger log = LoggerFactory.getLogger(UserProviderImpl.class);

    private final UserService userService;

    @Inject
    public UserProviderImpl(UserRepository repository, UserMapper mapper, UserService userService) {
        super(repository, mapper, org.jboss.pnc.model.User.class);
        this.userService = userService;
    }

    @Override
    public User getCurrentUser() {
        return mapper.toDTO(userService.currentUser());
    }

    /**
     * Not allowed
     *
     * @param id
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("Deleting users is prohibited");
    }
}
