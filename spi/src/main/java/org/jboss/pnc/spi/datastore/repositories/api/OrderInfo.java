/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories.api;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

public interface OrderInfo<T> {

    SortingDirection getDirection();

    Expression<?> getExpression(Root<T> root);

    enum SortingDirection {
        ASC, DESC
    }
}
