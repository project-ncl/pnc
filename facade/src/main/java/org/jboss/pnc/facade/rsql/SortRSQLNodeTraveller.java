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
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo.SortingDirection;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultSortInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.LogicalNode;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
class SortRSQLNodeTraveller<DB extends GenericEntity<Integer>> extends RSQLNodeTraveller<SortInfo> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Function<RSQLSelectorPath, String> toPath;

    public SortRSQLNodeTraveller(Function<RSQLSelectorPath, String> toPath) {
        this.toPath = toPath;
    }

    @Override
    public SortInfo visit(LogicalNode logicalNode) {
        return null;
    }

    @Override
    public SortInfo visit(ComparisonNode node) {
        SortingDirection sortingDirection;
        List<String> sortingFields = new ArrayList<>();

        if (node.getOperator().equals(ASC)) {
            sortingDirection = SortInfo.SortingDirection.ASC;
        } else if (node.getOperator().equals(DESC)) {
            sortingDirection = SortInfo.SortingDirection.DESC;
        } else {
            throw new UnsupportedOperationException("Unsupported sorting: " + node.getOperator());
        }

        logger.trace("Sorting direction - {}, arguments {}", sortingDirection, node.getArguments());
        for (String argument : node.getArguments()) {
            if ("id".equals(argument)) { // Disable sorting by id
                throw new RSQLException("Sorting by id is not supported.");
            }
            sortingFields.add(toPath.apply(RSQLSelectorPath.get(argument)));
        }
        return new DefaultSortInfo(sortingDirection, sortingFields);
    }
}
