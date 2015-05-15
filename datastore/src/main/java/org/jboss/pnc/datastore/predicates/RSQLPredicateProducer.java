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
package org.jboss.pnc.datastore.predicates;

import org.jboss.pnc.datastore.predicates.rsql.EmptyRSQLPredicate;
import org.jboss.pnc.datastore.predicates.rsql.RSQLNodeTravellerPredicate;

/**
 * Entry point for converting RSQL to Criteria.
 */
public interface RSQLPredicateProducer {

    /**
     * Converts RSQL to {@link org.springframework.data.jpa.domain.Specification}.
     *
     * @param query RSQL query - may be <code>null</code>.
     * @return New adapter.
     * @throws java.lang.IllegalArgumentException In case of parsing or converting exceptions.
     */
    public static <Entity> RSQLPredicate fromRSQL(Class<Entity> selectingClass, String query) {
        try {
            if(query == null || query.isEmpty()) {
                return new EmptyRSQLPredicate();
            }
            return new RSQLNodeTravellerPredicate<>(selectingClass, query);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse query: " + query, e);
        }
    }
}
