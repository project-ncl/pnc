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
package org.jboss.pnc.datastore.repositories.internal;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.OrderInfo;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
