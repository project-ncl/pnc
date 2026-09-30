/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories.api.impl;

import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

import org.jboss.pnc.spi.datastore.repositories.api.OrderInfo;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

/**
 * This class represents a {@link SortInfo} that has no defined sorting. Internally it uses a default stable sorting,
 * but otherwise is treated as an empty SortInfo.
 * 
 * @param <T>
 */
public class StableEmptySortInfo<T> implements SortInfo<T> {

    @Override
    public List<OrderInfo<T>> orders() {
        return Collections
                .singletonList(new DefaultOrderInfo<T>(OrderInfo.SortingDirection.ASC, StableEmptySortInfo::idOrder));
    }

    @Override
    public DefaultSortInfo<T> thenOrderBy(OrderInfo<T> order) {
        // remove default ID sorting on a change of ordering
        return new DefaultSortInfo<T>(order);
    }

    private static <T> Expression<?> idOrder(Root<T> root) {
        try {
            return root.get("id");
        } catch (IllegalArgumentException ex) {
            return root;
        }
    }
}
