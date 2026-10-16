/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories.api.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.metamodel.SingularAttribute;

import org.jboss.pnc.spi.datastore.repositories.api.OrderInfo;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

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
