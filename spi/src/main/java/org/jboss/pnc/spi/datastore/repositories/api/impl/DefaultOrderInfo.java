/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories.api.impl;

import java.util.function.Function;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.pnc.spi.datastore.repositories.api.OrderInfo;

public class DefaultOrderInfo<T> implements OrderInfo<T> {
    private final SortingDirection direction;
    private final Function<Root<T>, Expression<?>> toExpression;

    public DefaultOrderInfo(SortingDirection direction, Function<Root<T>, Expression<?>> toExpression) {
        this.direction = direction;
        this.toExpression = toExpression;
    }

    public DefaultOrderInfo(SortingDirection direction, SingularAttribute<T, ?> attribute) {
        this.direction = direction;
        this.toExpression = root -> root.get(attribute);
    }

    public static <T> DefaultOrderInfo<T> asc(SingularAttribute<T, ?> attribute) {
        return new DefaultOrderInfo<>(SortingDirection.ASC, attribute);
    }

    public static <T> DefaultOrderInfo<T> desc(SingularAttribute<T, ?> attribute) {
        return new DefaultOrderInfo<>(SortingDirection.DESC, attribute);
    }

    @Override
    public SortingDirection getDirection() {
        return direction;
    }

    @Override
    public Expression<?> getExpression(Root<T> root) {
        return toExpression.apply(root);
    }
}
