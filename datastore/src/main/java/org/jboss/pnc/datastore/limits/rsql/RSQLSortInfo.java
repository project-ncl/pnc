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
package org.jboss.pnc.datastore.limits.rsql;

import com.google.common.base.Preconditions;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.RSQLParserException;
import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.OrNode;
import cz.jirutka.rsql.parser.ast.RSQLVisitor;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RSQL implementation of Paging and Sorting
 */
public class RSQLSortInfo implements SortInfo {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String FIXED_START_OF_SORTING_EXPRESSION = "sort";

    private final List<String> sortingFields = new ArrayList<>();
    private SortingDirection sortingDirection = SortingDirection.ASC;

    private static final ComparisonOperator ASC = new ComparisonOperator("=asc=", true);
    private static final ComparisonOperator DESC = new ComparisonOperator("=desc=", true);

    public RSQLSortInfo(String sortingRsql) {
        Preconditions.checkArgument(
                sortingRsql != null && !sortingRsql.isEmpty(),
                "RSQL for sorting can't be null or empty");

        StringBuilder sortingRsqlBuilder = new StringBuilder(sortingRsql);
        if (!sortingRsql.startsWith(FIXED_START_OF_SORTING_EXPRESSION)) {
            sortingRsqlBuilder.insert(0, FIXED_START_OF_SORTING_EXPRESSION);
        }

        Set<ComparisonOperator> operators = new HashSet<>();
        operators.add(ASC);
        operators.add(DESC);

        RSQLParser rsqlParser = new RSQLParser(operators);
        try {
            // since we need to parse nodes (operand + operation + arguments, we have to append some work at the
            // beginning).
            Node parse = rsqlParser.parse(sortingRsqlBuilder.toString());
            parse.accept(new RSQLVisitor<Boolean, Void>() {
                @Override
                public Boolean visit(AndNode node, Void param) {
                    return null;
                }

                @Override
                public Boolean visit(OrNode node, Void param) {
                    return null;
                }

                @Override
                public Boolean visit(ComparisonNode node, Void param) {
                    if (node.getOperator().equals(ASC)) {
                        sortingDirection = SortingDirection.ASC;
                        sortingFields.addAll(node.getArguments());
                        logger.trace("Sorting direction - ASC, arguments {}", node.getArguments());
                    } else if (node.getOperator().equals(DESC)) {
                        sortingDirection = SortingDirection.DESC;
                        sortingFields.addAll(node.getArguments());
                        logger.trace("Sorting direction - DESC, arguments {}", node.getArguments());
                    } else {
                        throw new UnsupportedOperationException("Unsupported sorting: " + node.getOperator());
                    }
                    return true;
                }
            });
        } catch (RSQLParserException e) {
            throw new IllegalArgumentException("Could not parse sorting string", e);
        }
    }

    @Override
    public List<String> getFields() {
        return sortingFields;
    }

    @Override
    public SortingDirection getDirection() {
        return sortingDirection;
    }
}
