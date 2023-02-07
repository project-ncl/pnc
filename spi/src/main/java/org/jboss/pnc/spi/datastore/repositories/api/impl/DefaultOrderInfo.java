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

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.function.Function;

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

    @Override
    public SortingDirection getDirection() {
        return direction;
    }

    @Override
    public Expression<?> getExpression(Root<T> root) {
        return toExpression.apply(root);
    }
}
