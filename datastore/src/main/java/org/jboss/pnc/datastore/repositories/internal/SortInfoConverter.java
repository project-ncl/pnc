/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.repositories.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.OrderInfo;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

public class SortInfoConverter {

    public static <DB extends GenericEntity<?>> List<Order> toOrders(
            SortInfo<DB> sortInfo,
            Root<DB> from,
            CriteriaBuilder cb) {
        if (sortInfo == null) {
            return Collections.emptyList();
        }

        Objects.requireNonNull(from, "From must not be null");
        Objects.requireNonNull(cb, "CriteriaBuilder must not be null");

        List<Order> orders = new ArrayList<>();

        for (OrderInfo<DB> order : sortInfo.orders()) {
            orders.add(toJpaOrder(order, from, cb));
        }

        return orders;
    }

    private static <DB extends GenericEntity<?>> Order toJpaOrder(
            OrderInfo<DB> order,
            Root<DB> from,
            CriteriaBuilder cb) {
        Expression<?> expression = order.getExpression(from);
        return order.getDirection() == OrderInfo.SortingDirection.ASC ? cb.asc(expression) : cb.desc(expression);
    }
}
