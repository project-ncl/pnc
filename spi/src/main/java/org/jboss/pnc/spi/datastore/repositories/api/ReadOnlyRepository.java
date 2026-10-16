/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories.api;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.persistence.LockModeType;

import org.jboss.pnc.model.GenericEntity;

public interface ReadOnlyRepository<T extends GenericEntity<ID>, ID extends Serializable> {
    List<T> queryAll();

    List<T> queryAll(PageInfo pageInfo, SortInfo<T> sortInfo);

    default T queryById(ID id) {
        return queryById(id, LockModeType.NONE);
    }

    T queryById(ID id, LockModeType lockMode);

    T queryByPredicates(Predicate<T>... predicates);

    int count(Predicate<T>... predicates);

    int count(Collection<Predicate<T>> andPredicates, Collection<Predicate<T>> orPredicates);

    List<T> queryWithPredicates(Predicate<T>... predicates);

    List<T> queryWithPredicates(PageInfo pageInfo, SortInfo<T> sortInfo, Predicate<T>... predicates);
}
