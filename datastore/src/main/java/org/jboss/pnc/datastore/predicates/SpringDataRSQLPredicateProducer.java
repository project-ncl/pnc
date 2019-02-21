/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;

import javax.ejb.Stateless;

/**
 * Entry point for converting RSQL to Criteria.
 */
@Stateless
public class SpringDataRSQLPredicateProducer implements RSQLPredicateProducer {

    @Override
    public <T extends GenericEntity<? extends Number>> Predicate<T> getPredicate(Class<T> selectingClass, String rsql) {
        try {
            if(rsql == null || rsql.isEmpty()) {
                return new EmptyRSQLPredicate();
            }
            return new RSQLNodeTravellerPredicate<>(selectingClass, rsql).getEntityPredicate();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse query: " + rsql, e);
        }
    }

    @Override
    public <T> java.util.function.Predicate<T> getStreamPredicate(
            Class<T> selectingClass, String rsql) {
        try {
            if(rsql == null || rsql.isEmpty()) {
                return x -> true;
            }
            return new RSQLNodeTravellerPredicate<>(selectingClass, rsql).getStreamPredicate();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse query: " + rsql, e);
        }
    }

}
