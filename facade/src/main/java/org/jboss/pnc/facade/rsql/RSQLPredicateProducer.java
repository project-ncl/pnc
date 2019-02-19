/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.facade.rsql;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;

import java.util.function.BiFunction;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface RSQLPredicateProducer {

    /**
     * Creates a Criteria API predicate from RSQL query for DB search. It uses toPath function to
     * map RSQL selector to Criteria API Path. The predicate can throw a runtime exception when used
     * if the query is bad.
     * @param <T> Type of the entity.
     * @param toPath Function that converts RSQL selector to CriteriaAPI Path.
     * @param rsql The query RSQL.
     * @return Predicate representing the RSQL query.
     */
    <T extends GenericEntity<Integer>> Predicate<T> getCriteriaPredicate(BiFunction<From<?, T>, RSQLSelectorPath, Path> toPath, String rsql);

    /**
     * Creates a predicate from RSQL query for stream search. The predicate can throw a runtime
     * exception when used if the query is bad.
     * @param <T> Type of the entity.
     * @param rsql The query RSQL.
     * @return Predicate representing the RSQL query.
     */
    <T> java.util.function.Predicate<T> getStreamPredicate(String rsql);
}
