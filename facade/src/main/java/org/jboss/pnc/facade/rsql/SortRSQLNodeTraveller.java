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
package org.jboss.pnc.facade.rsql;

import static org.jboss.pnc.facade.rsql.RSQLProducerImpl.ASC;
import static org.jboss.pnc.facade.rsql.RSQLProducerImpl.DESC;

import cz.jirutka.rsql.parser.ast.Node;
import org.jboss.pnc.facade.rsql.mapper.RSQLMapper;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.OrderInfo;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.pnc.spi.datastore.repositories.api.OrderInfo.SortingDirection;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultOrderInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultSortInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.LogicalNode;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
class SortRSQLNodeTraveller<DB extends GenericEntity<Integer>> extends RSQLNodeTraveller<SortInfo<DB>> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
