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
package org.jboss.pnc.rest.provider;

import com.google.common.base.Preconditions;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.restmodel.UserRest;
import org.jboss.pnc.spi.datastore.predicates.UserPredicates;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.common.util.StringUtils.isEmpty;
import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@Stateless
public class UserProvider {

    private UserRepository userRepository;

    private RSQLPredicateProducer rsqlPredicateProducer;

    private SortInfoProducer sortInfoProducer;

    private PageInfoProducer pageInfoProducer;

    // needed for EJB/CDI
    public UserProvider() {
    }

    @Inject
    public UserProvider(UserRepository userRepository, RSQLPredicateProducer rsqlPredicateProducer, SortInfoProducer sortInfoProducer,
            PageInfoProducer pageInfoProducer) {
        this.userRepository = userRepository;
        this.rsqlPredicateProducer = rsqlPredicateProducer;
        this.sortInfoProducer = sortInfoProducer;
        this.pageInfoProducer = pageInfoProducer;
    }

    public List<UserRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        Predicate<User> rsqlPredicate = rsqlPredicateProducer.getPredicate(User.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);
        return nullableStreamOf(userRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public UserRest getSpecific(Integer userId) {
        User user = userRepository.queryById(userId);
        if (user != null) {
            return new UserRest(user);
        }
        return null;
    }

    public Integer store(UserRest userRest) {
        Preconditions.checkArgument(userRest.getId() == null, "Id must be null");
        Preconditions.checkArgument(!isEmpty(userRest.getUsername()), "Username is required");
        Preconditions.checkArgument(!isEmpty(userRest.getEmail()), "Email is required");
        checkForConflictingUser(userRest);

        User user = userRest.toUser();
        user = userRepository.save(user);
        return user.getId();
    }

    public Integer update(Integer id, UserRest userRest) {
        Preconditions.checkArgument(id != null, "Id must not be null");
        Preconditions.checkArgument(userRest.getId() == null || userRest.getId().equals(id),
                "Entity id does not match the id to update");
        Preconditions.checkArgument(!isEmpty(userRest.getUsername()), "Username is required");
        Preconditions.checkArgument(!isEmpty(userRest.getEmail()), "Email is required");
        userRest.setId(id);
        checkForConflictingUser(userRest);

        User user = userRepository.queryById(id);
        Preconditions.checkArgument(user != null, "Couldn't find user with id " + id);
        user = userRepository.save(userRest.toUser());
        return user.getId();
    }

    public Function<? super User, ? extends UserRest> toRestModel() {
        return user -> new UserRest(user);
    }

    /**
     * Check if the user object contains the required fields and does not conflict with
     * existing users in the database.
     * 
     * @throws IllegalArgumentException if the user is not valid
     * @param userRest
     */
    public void checkForConflictingUser(UserRest userRest) {
        List<User> conflictingUsers = userRepository.queryWithPredicates(UserPredicates.withUserName(userRest.getUsername()));
        conflictingUsers.addAll(userRepository.queryWithPredicates(UserPredicates.withEmail(userRest.getEmail())));
        for (User conflict : conflictingUsers) {
            if ( !conflict.getId().equals(userRest.getId()) ) {
                if ( userRest.getUsername().equals(conflict.getUsername())) {
                    throw new IllegalArgumentException("Username already in use by another user");
                }
                if ( userRest.getEmail().equals(conflict.getEmail())) {
                    throw new IllegalArgumentException("Email address already in use by another user");
                }
            }
        }
        return;
    }
}
