/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql;

import static org.jboss.pnc.facade.rsql.RSQLProducerImpl.ASC;
import static org.jboss.pnc.facade.rsql.RSQLProducerImpl.DESC;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.pnc.facade.rsql.mapper.RSQLMapper;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.OrderInfo;
import org.jboss.pnc.spi.datastore.repositories.api.OrderInfo.SortingDirection;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultOrderInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultSortInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.LogicalNode;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
class SortRSQLNodeTraveller<DB extends GenericEntity<Integer>> extends RSQLNodeTraveller<SortInfo<DB>> {

    private static final Logger logger = LoggerFactory.getLogger(SortRSQLNodeTraveller.class);

    private final RSQLMapper<?, DB> mapper;

    public SortRSQLNodeTraveller(RSQLMapper<?, DB> mapper) {
        this.mapper = mapper;
    }

    @Override
    public SortInfo<DB> visit(LogicalNode logicalNode) {
        List<OrderInfo<DB>> orders = logicalNode.getChildren()
                .stream()
                .flatMap(n -> visit(n).orders().stream())
                .collect(Collectors.toList());
        return new DefaultSortInfo<>(orders);
    }

    @Override
    public SortInfo<DB> visit(ComparisonNode node) {
        SortingDirection sortingDirection;

        if (node.getOperator().equals(ASC)) {
            sortingDirection = OrderInfo.SortingDirection.ASC;
        } else if (node.getOperator().equals(DESC)) {
            sortingDirection = OrderInfo.SortingDirection.DESC;
        } else {
            throw new UnsupportedOperationException("Unsupported sorting: " + node.getOperator());
        }

        logger.trace("Sorting direction - {}, arguments {}", sortingDirection, node.getArguments());
        List<OrderInfo<DB>> orders = new ArrayList<>();
        for (String argument : node.getArguments()) {
            RSQLSelectorPath path = RSQLSelectorPath.get(argument);
            RSQLSelectorPath last = path;
            while (!last.isFinal())
                last = last.next();
            if ("id".equals(last.getElement())) { // Disable sorting by id
                throw new RSQLException("Sorting by id is not supported.");
            }

            orders.add(new DefaultOrderInfo<>(sortingDirection, root -> mapper.toPath(root, path)));
        }
        return new DefaultSortInfo<>(orders);
    }
}
