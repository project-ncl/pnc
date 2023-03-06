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
package org.jboss.pnc.remotecoordinator.test.mock;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Alternative
@ApplicationScoped
public class UserRepositoryMock implements UserRepository {
    @Override
    public List<User> queryAll() {
        return null;
    }

    @Override
    public List<User> queryAll(PageInfo pageInfo, SortInfo<User> sortInfo) {
        return null;
    }

    @Override
    public User queryById(Integer id) {
        return null;
    }

    @Override
    public User queryByPredicates(Predicate<User>... predicates) {
        return null;
    }

    @Override
    public int count(Predicate<User>... predicates) {
        return 0;
    }

    @Override
    public int count(Collection<Predicate<User>> andPredicates, Collection<Predicate<User>> orPredicates) {
        return 0;
    }

    @Override
    public List<User> queryWithPredicates(Predicate<User>... predicates) {
        return null;
    }

    @Override
    public List<User> queryWithPredicates(PageInfo pageInfo, SortInfo<User> sortInfo, Predicate<User>... predicates) {
        return null;
    }

    @Override
    public User save(User entity) {
        return null;
    }

    @Override
    public void delete(Integer id) {
    }

    @Override
    public void delete(User id) {
    }

    @Override
    public void flushAndRefresh(User entity) {
    }

    @Override
    public <N extends GenericEntity<Integer>> void cascadeUpdates(
            N managedNonOwning,
            N updatedNonOwning,
            Function<N, Collection<User>> collectionGetter,
            BiConsumer<User, N> owningSetter,
            java.util.function.Predicate<User>... filters) {
    }
}
