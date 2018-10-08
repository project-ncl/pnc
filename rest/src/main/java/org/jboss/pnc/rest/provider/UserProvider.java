/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.provider;

import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.restmodel.UserRest;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.spi.datastore.predicates.UserPredicates;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.function.Function;

@Stateless
public class UserProvider extends AbstractProvider<User, UserRest> {

    // needed for EJB/CDI
    public UserProvider() {
    }

    @Inject
    public UserProvider(UserRepository userRepository, RSQLPredicateProducer rsqlPredicateProducer, SortInfoProducer sortInfoProducer,
            PageInfoProducer pageInfoProducer) {
        super(userRepository, rsqlPredicateProducer, sortInfoProducer, pageInfoProducer);
    }

    @Override
    protected Function<? super User, ? extends UserRest> toRESTModel() {
        return user -> new UserRest(user);
    }

    @Override
    protected Function<? super UserRest, ? extends User> toDBModel() {
        return user -> user.toDBEntityBuilder().build();
    }

    @Override
    protected void validateBeforeSaving(UserRest restEntity) throws RestValidationException {


        if(repository.count(UserPredicates.withEmail(restEntity.getEmail())) != 0) {
            throw new IllegalArgumentException("Email address already in use by another user");
        }

        if(repository.count(UserPredicates.withUserName(restEntity.getUsername())) != 0) {
            throw new IllegalArgumentException("Username already in use by another user");
        }
    }
}
