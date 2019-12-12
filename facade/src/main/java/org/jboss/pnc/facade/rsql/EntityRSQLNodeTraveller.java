/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

import static org.jboss.pnc.facade.rsql.RSQLProducerImpl.IS_NULL;
import static org.jboss.pnc.facade.rsql.RSQLProducerImpl.LIKE;

import org.jboss.pnc.model.GenericEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

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
class EntityRSQLNodeTraveller<DB extends GenericEntity<Integer>> extends RSQLNodeTraveller<javax.persistence.criteria.Predicate> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Root<DB> root;
    private final CriteriaBuilder cb;
    private final BiFunction<From<?, DB>, RSQLSelectorPath, Path> toPath;

    public EntityRSQLNodeTraveller(Root<DB> root, CriteriaBuilder cb, BiFunction<From<?, DB>, RSQLSelectorPath, Path> toPath) {
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
            Object argument = cast(arguments.get(0), path.getJavaType());
            return cb.equal(path, argument);
        } else if (RSQLOperators.NOT_EQUAL.equals(operator)) {
            Object argument = cast(arguments.get(0), path.getJavaType());
            return cb.notEqual(path, argument);
        } else if (RSQLOperators.GREATER_THAN.equals(operator)) {
            Comparable argument = cast(arguments.get(0), path.getJavaType());
            return cb.greaterThan(path, argument);
        } else if (RSQLOperators.GREATER_THAN_OR_EQUAL.equals(operator)) {
            Comparable argument = cast(arguments.get(0), path.getJavaType());
            return cb.greaterThanOrEqualTo(path, argument);
        } else if (RSQLOperators.LESS_THAN.equals(operator)) {
            Comparable argument = cast(arguments.get(0), path.getJavaType());
            return cb.lessThan(path, argument);
        } else if (RSQLOperators.LESS_THAN_OR_EQUAL.equals(operator)) {
            Comparable argument = cast(arguments.get(0), path.getJavaType());
            return cb.lessThanOrEqualTo(path, argument);
        } else if (RSQLOperators.IN.equals(operator)) {
            List<Object> castArguments = castArguments(arguments, path.getJavaType());
            return path.in(castArguments);
        } else if (RSQLOperators.NOT_IN.equals(operator)) {
            List<Object> castArguments = castArguments(arguments, path.getJavaType());
            return cb.not(path.in(castArguments));
        } else if (LIKE.equals(operator)) {
            return cb.like(cb.lower(path), preprocessLikeOperatorArgument(arguments.get(0).toLowerCase()));
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

    private String preprocessLikeOperatorArgument(String argument) {
        return argument.replaceAll("\\?", "_").replaceAll("\\*", "%");
    }

    private <T extends Comparable<? super T>> List<T> castArguments(List<String> arguments, Class<T> javaType) {
        if (javaType == String.class) {
            return (List<T>) arguments;
        }
        return arguments.stream().map(a -> cast(a, javaType)).collect(Collectors.toList());
    }

    private <T extends Comparable<? super T>> T cast(String argument, Class<T> javaType) {
        if (javaType.isEnum()) {
            Class<? extends Enum> enumType = (Class<? extends Enum>) javaType;
            return (T) Enum.valueOf(enumType, argument);
        } else if (javaType == String.class) {
            return (T) argument;
        } else if (javaType == Integer.class || javaType == int.class) {
            return (T) Integer.valueOf(argument);
        } else if (javaType == Long.class || javaType == long.class) {
            return (T) Long.valueOf(argument);
        } else if (javaType == Boolean.class || javaType == boolean.class) {
            return (T) Boolean.valueOf(argument);
        } else if (javaType == Date.class) {
            try {
                DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
                OffsetDateTime offsetDateTime = OffsetDateTime.parse(argument, timeFormatter);
                return (T) Date.from(Instant.from(offsetDateTime));
            } catch (DateTimeParseException ex) {
                throw new RSQLException("The datetime must be in the ISO-8601 format with timezone, e.g. 1970-01-01T00:00:00Z, was " + argument, ex);
            }
        } else {
            throw new UnsupportedOperationException("The target type " + javaType + " is not known to the type converter.");
        }
    }

}