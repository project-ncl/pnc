/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories.api;

import java.util.List;

public interface SortInfo<T> {
    List<OrderInfo<T>> orders();

    SortInfo<T> thenOrderBy(OrderInfo<T> order);
}
