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
package org.jboss.pnc.mapper;

import org.jboss.pnc.mapper.api.ByUsername;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import static org.jboss.pnc.spi.datastore.predicates.UserPredicates.withUserName;

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
