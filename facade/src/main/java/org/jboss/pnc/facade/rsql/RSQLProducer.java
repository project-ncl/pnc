/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql;

import java.util.Comparator;

import org.jboss.pnc.facade.rsql.mapper.RSQLMapper;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface RSQLProducer {

    /**
     * See {@link this#getCriteriaPredicate(RSQLMapper, String)}. However, the RSQL mapper is automatically selected
     * using the given type.
     *
     * @param <DB> Type of the entity.
     * @param type Entity type.
     * @param rsql The query RSQL.
     * @return Predicate representing the RSQL query.
     */
    <DB extends GenericEntity<?>> Predicate<DB> getCriteriaPredicate(Class<DB> type, String rsql);

    /**
     * Creates a Criteria API predicate from RSQL query for DB search. It uses provided mapper to map RSQL selector to
     * Criteria API path. The predicate can throw a runtime exception when used if the query is bad.
     *
     * @param <DB> Type of the entity.
     * @param mapper RSQL mapper to be used.
     * @param rsql The query RSQL.
     */
    <DB extends GenericEntity<?>> Predicate<DB> getCriteriaPredicate(RSQLMapper<?, DB> mapper, String rsql);

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
     * See {@link this#getSortInfo(RSQLMapper, String)}. However, the RSQL mapper is automatically selected using the
     * given type.
     *
     * @param <DB> Type of the entity.
     * @param type Entity type.
     * @param rsql query for sorting, e.g. <code>"=asc=id"</code>.
     * @return Sort Info object.
     */
    <DB extends GenericEntity<?>> SortInfo<DB> getSortInfo(Class<DB> type, String rsql);

    /**
     * Creates a {@link SortInfo} from RSQL query for DB search. It uses provided mapper to map RSQL selector to
     * Criteria API path.
     *
     * @param <DB> Type of the entity.
     * @param mapper RSQL mapper to be used.
     * @param rsql query for sorting, e.g. <code>"=asc=id"</code>.
     */
    <DB extends GenericEntity<?>> SortInfo<DB> getSortInfo(RSQLMapper<?, DB> mapper, String rsql);

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
