/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import java.io.Serializable;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;

import org.jboss.pnc.facade.rsql.RSQLSelectorPath;
import org.jboss.pnc.facade.rsql.converter.ValueConverter;
import org.jboss.pnc.model.GenericEntity;

/**
 * Mappers that converts RSQL path with DTO field names to Criteria API path.
 * 
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @param <DB> The database entity type
 */
public interface RSQLMapper<ID extends Serializable, DB extends GenericEntity<ID>> {

    /**
     * Return the class for which this mapper works.
     * 
     * @return
     */
    Class<DB> type();

    /**
     * Converts RSQL selector to Criteria API path.
     * 
     * @param from The entity path node.
     * @param selector The RSQL selector.
     * @return Criteria API path equivalent of the RSQL selector.
     */
    Path<?> toPath(From<?, DB> from, RSQLSelectorPath selector);

    /**
     * Converts RSQL selector to string with '.' separetaed list of entity field names.
     * 
     * @param selector The RSQL selector
     * @return String with entity field names separated by '.'.
     */
    String toPath(RSQLSelectorPath selector);

    ValueConverter getValueConverter(String name);
}
