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
package org.jboss.pnc.datastore.limits.rsql;

import org.jboss.pnc.spi.datastore.repositories.api.OrderInfo;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultOrderInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultSortInfo;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class EmptySortInfo<T> extends DefaultSortInfo<T> {

    public EmptySortInfo() {
        super(new DefaultOrderInfo<T>(OrderInfo.SortingDirection.ASC, EmptySortInfo::idOrder));
    }

    @Override
    public DefaultSortInfo<T> thenOrderBy(SingularAttribute<T, ?> attribute, OrderInfo.SortingDirection direction) {
        // remove default ID sorting on a change of ordering
        return new DefaultSortInfo<T>().thenOrderBy(attribute, direction);
    }

    private static <T> Expression<?> idOrder(Root<T> root) {
        try {
            return root.get("id");
        } catch (IllegalArgumentException ex) {
            return root;
        }
    }
}
