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
package org.jboss.pnc.datastore.repositories.internal;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpecificationsMapper {

    public static <T extends GenericEntity<? extends Serializable>> Specification<T> map(Predicate<T>... predicates) {
        return (root, query, cb) -> {
            List<javax.persistence.criteria.Predicate> jpaPredicates = Stream.of(predicates)
                    .map(predicate -> predicate.apply(root, query, cb))
                    .collect(Collectors.toList());
            return cb.and(jpaPredicates.toArray(new javax.persistence.criteria.Predicate[0]));
        };
    }

    public static <T extends GenericEntity<? extends Serializable>> Specification<T> map(
            Collection<Predicate<T>> andPredicates,
            Collection<Predicate<T>> orPredicates) {
        return (root, query, cb) -> {
            List<javax.persistence.criteria.Predicate> jpaPredicatesAnd = andPredicates.stream()
                    .map(predicate -> predicate.apply(root, query, cb))
                    .collect(Collectors.toList());

            List<javax.persistence.criteria.Predicate> jpaPredicatesOr = orPredicates.stream()
                    .map(predicate -> predicate.apply(root, query, cb))
                    .collect(Collectors.toList());

            if (andPredicates.isEmpty()) {
                if (orPredicates.isEmpty())
                    return cb.conjunction();
                else {
                    // If andPredicates are empty, that part needs to result in false otherwise the predicate will be
                    // always true (true OR anything => true)
                    return cb.or(
                            cb.disjunction(),
                            cb.or(jpaPredicatesOr.toArray(new javax.persistence.criteria.Predicate[0])));
                }
            } else
                return cb.or(
                        cb.and(jpaPredicatesAnd.toArray(new javax.persistence.criteria.Predicate[0])),
                        cb.or(jpaPredicatesOr.toArray(new javax.persistence.criteria.Predicate[0])));
        };
    }

}
