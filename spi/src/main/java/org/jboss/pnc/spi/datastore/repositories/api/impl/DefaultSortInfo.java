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
package org.jboss.pnc.spi.datastore.repositories.api.impl;

import org.jboss.pnc.spi.datastore.repositories.api.OrderInfo;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultSortInfo<T> implements SortInfo<T> {
    private final List<OrderInfo<T>> order;

    public DefaultSortInfo(List<OrderInfo<T>> orderInfo) {
        this.order = new ArrayList<>(orderInfo);
    }

    public DefaultSortInfo(OrderInfo<T> orderInfo) {
        this(Collections.singletonList(orderInfo));
    }

    public DefaultSortInfo() {
        this.order = new ArrayList<>();
    }

    public static <T> SortInfo<T> asc(SingularAttribute<T, ?> field) {
        DefaultOrderInfo<T> orderInfo = new DefaultOrderInfo<>(
                OrderInfo.SortingDirection.ASC,
                (Root<T> root) -> root.get(field));
        return new DefaultSortInfo<>(orderInfo);
    }

    public static <T> SortInfo<T> desc(SingularAttribute<T, ?> field) {
        DefaultOrderInfo<T> orderInfo = new DefaultOrderInfo<>(
                OrderInfo.SortingDirection.DESC,
                (Root<T> root) -> root.get(field));
        return new DefaultSortInfo<>(orderInfo);
    }

    public static <T> SortInfo<T> descs(SingularAttribute<T, ?>... fields) {
        return new DefaultSortInfo<T>().appendAll(OrderInfo.SortingDirection.DESC, fields);
    }

    public static <T> SortInfo<T> ascs(SingularAttribute<T, ?>... fields) {
        return new DefaultSortInfo<T>().appendAll(OrderInfo.SortingDirection.ASC, fields);
    }

    private DefaultSortInfo<T> appendAll(OrderInfo.SortingDirection dir, SingularAttribute<T, ?>... fields) {
        if (fields != null) {
            for (SingularAttribute<T, ?> attribute : fields) {
                order.add(new DefaultOrderInfo<>(dir, root -> root.get(attribute)));
            }
        }

        return this;
    }

    public DefaultSortInfo<T> thenOrderBy(SingularAttribute<T, ?> attribute, OrderInfo.SortingDirection direction) {
        return appendAll(direction, attribute);
    }

    @Override
    public List<OrderInfo<T>> orders() {
        return Collections.unmodifiableList(order);
    }
}
