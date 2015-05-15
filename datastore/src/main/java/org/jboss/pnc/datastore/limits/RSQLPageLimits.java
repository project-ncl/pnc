/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.datastore.limits;

import com.google.common.base.Preconditions;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.RSQLParserException;
import cz.jirutka.rsql.parser.ast.Node;
import org.jboss.pnc.datastore.limits.rsql.AscendingSortingNode;
import org.jboss.pnc.datastore.limits.rsql.DescendingSortingNode;
import org.jboss.pnc.datastore.limits.rsql.SortingRSQLNodesFactory;
import org.jboss.pnc.datastore.limits.rsql.SortingRSQLVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

/**
 * RSQL implementation of Paging and Sorting
 */
public class RSQLPageLimits extends EmptyPageLimits {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String FIXED_START_OF_SORTING_EXPRESSION = "sort";

    private final List<String> sortingFields = new ArrayList<>();
    private Sort.Direction sortingDirection = Sort.DEFAULT_DIRECTION;

    public RSQLPageLimits(int size, int offset, String sortingRsql) {
        super(size, offset);
        Preconditions.checkArgument(sortingRsql != null && !sortingRsql.isEmpty(), "RSQL for sorting can't be null or empty");

        StringBuilder sortingRsqlBuilder = new StringBuilder(sortingRsql);
        if(!sortingRsql.startsWith(FIXED_START_OF_SORTING_EXPRESSION)) {
            sortingRsqlBuilder.insert(0, FIXED_START_OF_SORTING_EXPRESSION);
        }

        RSQLParser rsqlParser = new RSQLParser(new SortingRSQLNodesFactory());
        try {
            //since we need to parse nodes (operand + operation + arguments, we have to append some work at the beginning).
            Node parse = rsqlParser.parse(sortingRsqlBuilder.toString());
            parse.accept(new SortingRSQLVisitor<Boolean, Void>() {
                @Override public Boolean visit(AscendingSortingNode node, Void param) {
                    sortingDirection = Sort.Direction.ASC;
                    sortingFields.addAll(node.getArguments());
                    logger.debug("Sorting direction - ASC, arguments {}", node.getArguments());
                    return true;
                }

                @Override public Boolean visit(DescendingSortingNode node, Void param) {
                    sortingDirection = Sort.Direction.DESC;
                    sortingFields.addAll(node.getArguments());
                    logger.debug("Sorting direction - DESC, arguments {}", node.getArguments());
                    return true;
                }
            });
        } catch (RSQLParserException e) {
            throw new IllegalArgumentException("Could not parse sorting string", e);
        }
    }

    @Override
    public PageRequest toPageRequest() {
        return new PageRequest(pageOffset, pageLimit, sortingDirection, sortingFields.toArray(new String[0]));
    }
}
