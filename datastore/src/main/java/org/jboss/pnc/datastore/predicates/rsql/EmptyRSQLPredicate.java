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
package org.jboss.pnc.datastore.predicates.rsql;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Empty implementation of a RSQL adapter
 *
 * <p>
 * Converts RSQL query into Spring Data's {@link org.springframework.data.jpa.domain.Specification}, which in turn might
 * be used for selecting records.
 * </p>
 */
public class EmptyRSQLPredicate implements org.jboss.pnc.spi.datastore.repositories.api.Predicate {

    @Override
    public Predicate apply(Root root, CriteriaQuery query, CriteriaBuilder cb) {
        return cb.conjunction();
    }

}
