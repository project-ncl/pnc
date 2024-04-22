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
package org.jboss.pnc.spi.datastore.repositories.api;

import org.jboss.pnc.model.BuildRecord;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
