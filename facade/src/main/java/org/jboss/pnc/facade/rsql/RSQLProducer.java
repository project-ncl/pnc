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
package org.jboss.pnc.facade.rsql;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import java.util.Comparator;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface RSQLProducer {

    /**
     * Creates a Criteria API predicate from RSQL query for DB search. It uses toPath function to map RSQL selector to
     * Criteria API Path. The predicate can throw a runtime exception when used if the query is bad.
     * 
     * @param <DB> Type of the entity.
     * @param type Entity type.
     * @param rsql The query RSQL.
     * @return Predicate representing the RSQL query.
     */
    <DB extends GenericEntity<?>> Predicate<DB> getCriteriaPredicate(Class<DB> type, String rsql);

    /**
     * Creates a predicate from RSQL query for stream search. The predicate can throw a runtime exception when used if
     * the query is bad.
     * 
     * @param <DTO> Type of the entity.
     * @param rsql The query RSQL.
     * @return Predicate representing the RSQL query.
     */
    <DTO> java.util.function.Predicate<DTO> getStreamPredicate(String rsql);

    /**
     * Gets sort info based on RSQL query.
     *
     * @param <DB> Type of the entity.
     * @param type Entity type.
     * @param rsql query for sorting, e.g. <code>"=asc=id"</code>.
     * @return Sort Info object.
     */
    <DB extends GenericEntity<?>> SortInfo getSortInfo(Class<DB> type, String rsql);

    /**
     * Gets comparator based on RSQL query.
     *
     * @param <DTO> Type of the entity.
     * @param rsql query for sorting, e.g. <code>"=asc=id"</code>.
     * @return Comparator baset of the RSQL query.
     * @throws IllegalArgumentException when the rsql query is null or empty.
     */
    <DTO> Comparator<DTO> getComparator(String rsql);
}
