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

import org.jboss.pnc.facade.rsql.RSQLException;
import org.jboss.pnc.facade.rsql.RSQLSelectorPath;
import org.jboss.pnc.facade.rsql.converter.CastValueConverter;
import org.jboss.pnc.facade.rsql.converter.ValueConverter;
import org.jboss.pnc.model.GenericEntity;

import javax.inject.Inject;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public abstract class AbstractRSQLMapper<ID extends Serializable, DB extends GenericEntity<ID>>
        implements RSQLMapper<ID, DB> {

    @Inject
    private UniversalRSQLMapper mapper;

    private final Class<DB> type;

    public AbstractRSQLMapper(Class<DB> type) {
        this.type = type;
    }

    private ValueConverter defaultConverter = new CastValueConverter();

    @Override
    public Class<DB> type() {
        return type;
    }

    @Override
    public Path<?> toPath(From<?, DB> from, RSQLSelectorPath selector) {
        String name = selector.getElement();
        if (toAttribute(name) != null) {
            return from.get(toAttribute(name));
        }
        if (toEntity(name) != null) {
            if (selector.isFinal()) {
                return from.get(toEntity(name));
            } else {
                return mapEntity(from, toEntity(name), selector.next());
            }
        }
        throw new RSQLException("Unknown RSQL selector " + name + " for type " + type);
    }

    protected <X extends GenericEntity<?>> Path<?> mapEntity(
            From<?, DB> from,
            SingularAttribute<DB, X> entity,
            RSQLSelectorPath selector) {
        Class<X> bindableJavaType = entity.getBindableJavaType();
        From<DB, X> join = from.join(entity);
        return mapper.toPath(bindableJavaType, join, selector);
    }

    @Override
    public String toPath(RSQLSelectorPath selector) {
        String name = selector.getElement();
        if (toAttribute(name) != null) {
            return toAttribute(name).getName();
        }
        SingularAttribute<DB, ? extends GenericEntity<?>> entity = toEntity(name);
        if (entity != null) {
            Class<? extends GenericEntity<?>> bindableType = entity.getBindableJavaType();
            return entity.getName() + "." + mapper.toPath(bindableType, selector.next());
        }
        throw new RSQLException("Unknown RSQL selector " + name + " for type " + type);
    }

    @Override
    public ValueConverter getValueConverter(String allNames) {
        return defaultConverter;
    }

    protected abstract SingularAttribute<DB, ? extends GenericEntity<?>> toEntity(String name);

    protected abstract SingularAttribute<DB, ?> toAttribute(String name);

}
