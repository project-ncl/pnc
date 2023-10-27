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

import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultSortInfo<T> implements SortInfo<T> {
    private final List<OrderInfo<T>> order; // immutable list

    public DefaultSortInfo(List<OrderInfo<T>> orderInfo) {
        this.order = List.copyOf(orderInfo);
    }

    public DefaultSortInfo(OrderInfo<T> orderInfo) {
        this(Collections.singletonList(orderInfo));
    }

    public static <T> SortInfo<T> asc(SingularAttribute<T, ?> field) {
        DefaultOrderInfo<T> orderInfo = DefaultOrderInfo.asc(field);
        return new DefaultSortInfo<>(orderInfo);
    }

    public static <T> SortInfo<T> desc(SingularAttribute<T, ?> field) {
        DefaultOrderInfo<T> orderInfo = DefaultOrderInfo.desc(field);
        return new DefaultSortInfo<>(orderInfo);
    }

    public static <T> SortInfo<T> asc(SingularAttribute<T, ?>... fields) {
        List<DefaultOrderInfo<T>> order = Arrays.stream(fields).map(DefaultOrderInfo::asc).collect(Collectors.toList());
        return new DefaultSortInfo(order);
    }

    public static <T> SortInfo<T> desc(SingularAttribute<T, ?>... fields) {
        List<DefaultOrderInfo<T>> order = Arrays.stream(fields)
                .map(DefaultOrderInfo::desc)
                .collect(Collectors.toList());
        return new DefaultSortInfo(order);
    }

    @Override
    public SortInfo<T> thenOrderBy(OrderInfo<T> order) {
        ArrayList<OrderInfo<T>> arrayList = new ArrayList<>(this.orders());
        arrayList.add(order);
        return new DefaultSortInfo<>(arrayList);
    }

    @Override
    public List<OrderInfo<T>> orders() {
        return order;
    }
}
