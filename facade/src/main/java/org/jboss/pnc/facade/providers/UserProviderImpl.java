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
package org.jboss.pnc.facade.providers;

import org.jboss.pnc.dto.User;
import org.jboss.pnc.mapper.api.UserMapper;
import org.jboss.pnc.facade.providers.api.UserProvider;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;

import static org.jboss.pnc.spi.datastore.predicates.UserPredicates.withUserName;

@PermitAll
@Stateless
public class UserProviderImpl
        extends AbstractIntIdProvider<org.jboss.pnc.model.User, User, User> implements UserProvider {

    private static final Logger log = LoggerFactory.getLogger(UserProviderImpl.class);

    @Inject
    public UserProviderImpl(UserRepository repository,
                            UserMapper mapper) {
        super(repository, mapper, org.jboss.pnc.model.User.class);
    }

    @Override
    public User getOrCreateNewUser(String username) {

        User currentUser = mapper.toDTO(repository.queryByPredicates(withUserName(username)));

        if (currentUser == null) {

            log.debug("Adding new user '{}' in database", username);
            currentUser = User.builder().username(username).build();
            store(currentUser);
        }

        return currentUser;
    }

    /**
     * Not allowed
     * @param id
     * @param user
     *
     * @return
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public User update(String id, User user) {
        throw new UnsupportedOperationException("Updating users is prohibited");
    }

    /**
     * Not allowed
     * @param id
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("Deleting users is prohibited");
    }
}
