/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.facade.rsql.mapper;

import org.jboss.pnc.facade.rsql.RSQLSelectorPath;
import org.jboss.pnc.facade.rsql.converter.Value;
import org.jboss.pnc.facade.rsql.converter.ValueConverter;
import org.jboss.pnc.model.GenericEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;

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

    private <DB extends GenericEntity<?>> RSQLMapper<?, DB> mapper(Class<DB> type) {
        for (RSQLMapper<?, ?> mapper : mappers) {
            if (mapper.type() == type) {
                return (RSQLMapper<?, DB>) mapper;
            }
        }
        throw new UnsupportedOperationException("Missing RSQL mapper implementation for " + type);
    }

    public <DB extends GenericEntity<?>, T> Comparable<T> convertValue(Value<DB, T> value) {
        ValueConverter valueConverter = mapper(value.getModelClass()).getValueConverter(value.getName());
        return valueConverter.convert(value);
    }
}
