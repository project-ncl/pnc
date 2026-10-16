/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;

import org.jboss.pnc.facade.rsql.RSQLSelectorPath;
import org.jboss.pnc.facade.rsql.converter.Value;
import org.jboss.pnc.facade.rsql.converter.ValueConverter;
import org.jboss.pnc.model.GenericEntity;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class UniversalRSQLMapper {

    @Inject
    private Instance<RSQLMapper<?, ?>> mappers;

    public <DB extends GenericEntity<?>> Path<?> toPath(Class<DB> type, From<?, DB> from, RSQLSelectorPath selector) {
        return mapper(type).toPath(from, selector);
    }

    public <DB extends GenericEntity<?>> String toPath(Class<DB> type, RSQLSelectorPath selector) {
        return mapper(type).toPath(selector);
    }

    public <DB extends GenericEntity<?>> RSQLMapper<?, DB> mapper(Class<DB> type) {
        for (RSQLMapper<?, ?> mapper : mappers) {
            if (mapper.type() == type) {
                return (RSQLMapper<?, DB>) mapper;
            }
        }
        throw new UnsupportedOperationException("Missing RSQL mapper implementation for " + type);
    }

    public ValueConverter getConverter() {
        return new UniversalValueConverter();
    }

    public class UniversalValueConverter implements ValueConverter {

        public <DB extends GenericEntity<?>, T> Comparable<T> convertComparable(Value<DB, T> value) {
            return mapper(value.getModelClass()).getValueConverter(value.getName()).convertComparable(value);
        }

        public <DB extends GenericEntity<?>, T> T convert(Value<DB, T> value) {
            return mapper(value.getModelClass()).getValueConverter(value.getName()).convert(value);
        }
    }
}
