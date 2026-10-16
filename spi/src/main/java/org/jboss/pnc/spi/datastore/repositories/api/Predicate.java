/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories.api;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public interface Predicate<T> {
    javax.persistence.criteria.Predicate apply(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);

    static <T> Predicate<T> nonMatching() {
        return (root, query, cb) -> cb.disjunction();
    }

    static <T> Predicate<T> and(Iterable<Predicate<T>> predicates) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            javax.persistence.criteria.Predicate[] array = StreamSupport.stream(predicates.spliterator(), false)
                    .map(k -> k.apply(root, query, cb))
                    .collect(Collectors.toList())
                    .toArray(javax.persistence.criteria.Predicate[]::new);
            return cb.and(array);
        };
    }
}
