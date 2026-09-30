/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.test.mock;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.persistence.LockModeType;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

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
    public User queryById(Integer id, LockModeType lockMode) {
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
