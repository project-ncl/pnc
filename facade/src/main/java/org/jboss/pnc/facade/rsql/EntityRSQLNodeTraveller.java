/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

import static org.jboss.pnc.facade.rsql.RSQLPredicateProducerImpl.IS_NULL;
import static org.jboss.pnc.facade.rsql.RSQLPredicateProducerImpl.LIKE;
import org.jboss.pnc.model.GenericEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.LogicalNode;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.OrNode;
import cz.jirutka.rsql.parser.ast.RSQLOperators;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
class EntityRSQLNodeTraveller<T extends GenericEntity<Integer>> extends RSQLNodeTraveller<javax.persistence.criteria.Predicate> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Root<T> root;
    private final CriteriaBuilder cb;
    private final BiFunction<From<?, T>, RSQLSelectorPath, Path> toPath;

    public EntityRSQLNodeTraveller(Root<T> root, CriteriaBuilder cb, BiFunction<From<?, T>, RSQLSelectorPath, Path> toPath) {
        this.root = root;
        this.cb = cb;
        this.toPath = toPath;
    }

    @Override
    public javax.persistence.criteria.Predicate visit(LogicalNode node) {
        logger.trace("Parsing LogicalNode {}", node);
        return proceedEmbeddedNodes(node);
    }

    @Override
    public javax.persistence.criteria.Predicate visit(ComparisonNode node) {
        logger.trace("Parsing ComparisonNode {}", node);
        return proceedSelection(node);
    }

    private javax.persistence.criteria.Predicate proceedSelection(ComparisonNode node) {
        RSQLSelectorPath selector = RSQLSelectorPath.get(node.getSelector());
        final Path path = toPath.apply(root, selector);
        List<String> arguments = node.getArguments();
        final ComparisonOperator operator = node.getOperator();
        if (RSQLOperators.EQUAL.equals(operator)) {
            return cb.equal(path, arguments.get(0));
        } else if (RSQLOperators.NOT_EQUAL.equals(operator)) {
            return cb.notEqual(path, arguments.get(0));
        } else if (RSQLOperators.GREATER_THAN.equals(operator)) {
            return cb.greaterThan(path, arguments.get(0));
        } else if (RSQLOperators.GREATER_THAN_OR_EQUAL.equals(operator)) {
            return cb.greaterThanOrEqualTo(path, arguments.get(0));
        } else if (RSQLOperators.LESS_THAN.equals(operator)) {
            return cb.lessThan(path, arguments.get(0));
        } else if (RSQLOperators.LESS_THAN_OR_EQUAL.equals(operator)) {
            return cb.lessThanOrEqualTo(path, arguments.get(0));
        } else if (RSQLOperators.IN.equals(operator)) {
            return path.in(arguments);
        } else if (RSQLOperators.NOT_IN.equals(operator)) {
            return cb.not(path.in(arguments));
        } else if (LIKE.equals(operator)) {
            return cb.like(cb.lower(path), arguments.get(0).toLowerCase());
        } else if (IS_NULL.equals(operator)) {
            if (Boolean.parseBoolean(arguments.get(0))) {
                return cb.isNull(path);
            } else {
                return cb.isNotNull(path);
            }
        } else {
            throw new UnsupportedOperationException("Not Implemented yet!");
        }
    }

    private javax.persistence.criteria.Predicate proceedEmbeddedNodes(LogicalNode node) {
        Iterator<Node> iterator = node.iterator();
        javax.persistence.criteria.Predicate p1 = visit(iterator.next());
        javax.persistence.criteria.Predicate p2 = visit(iterator.next());
        if (node instanceof AndNode) {
            javax.persistence.criteria.Predicate pCombined = cb.and(p1, p2);
            while (iterator.hasNext()) {
                javax.persistence.criteria.Predicate pNext = visit(iterator.next());
                pCombined = cb.and(pCombined, pNext);
            }
            return pCombined;
        } else if (node instanceof OrNode) {
            javax.persistence.criteria.Predicate pCombined = cb.or(p1, p2);
            while (iterator.hasNext()) {
                javax.persistence.criteria.Predicate pNext = visit(iterator.next());
                pCombined = cb.or(pCombined, pNext);
            }
            return pCombined;
        } else {
            throw new UnsupportedOperationException("Logical operation not supported");
        }
    }
}
