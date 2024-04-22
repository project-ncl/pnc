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

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.LogicalNode;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.OrNode;
import cz.jirutka.rsql.parser.ast.RSQLOperators;
import org.hibernate.query.criteria.internal.path.SingularAttributePath;
import org.jboss.pnc.facade.rsql.converter.Value;
import org.jboss.pnc.facade.rsql.converter.ValueConverter;
import org.jboss.pnc.facade.rsql.mapper.RSQLMapper;
import org.jboss.pnc.model.GenericEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jboss.pnc.facade.rsql.RSQLProducerImpl.IS_NULL;
import static org.jboss.pnc.facade.rsql.RSQLProducerImpl.LIKE;
import static org.jboss.pnc.facade.rsql.RSQLProducerImpl.NOT_LIKE;
import static org.jboss.pnc.facade.rsql.RSQLProducerImpl.WILDCARD_MULTIPLE_CHARACTERS;
import static org.jboss.pnc.facade.rsql.RSQLProducerImpl.WILDCARD_SINGLE_CHARACTER;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
class EntityRSQLNodeTraveller<DB extends GenericEntity<Integer>>
        extends RSQLNodeTraveller<javax.persistence.criteria.Predicate> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final char ESCAPE_CHAR = '\\';

    private final Root<DB> root;
    private final CriteriaBuilder cb;
    private final RSQLMapper<?, DB> mapper;
    private ValueConverter valueConverter;

    public EntityRSQLNodeTraveller(
            Root<DB> root,
            CriteriaBuilder cb,
            RSQLMapper<?, DB> mapper,
            ValueConverter valueConverter) {
        this.root = root;
        this.cb = cb;
        this.mapper = mapper;
        this.valueConverter = valueConverter;
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
        final Path path = mapper.toPath(root, selector);
        List<String> arguments = node.getArguments();
        final ComparisonOperator operator = node.getOperator();
        if (RSQLOperators.EQUAL.equals(operator)) {
            Object argument = valueConverter.convert(getValue(path, arguments.get(0)));
            return cb.equal(path, argument);
        } else if (RSQLOperators.NOT_EQUAL.equals(operator)) {
            Object argument = valueConverter.convert(getValue(path, arguments.get(0)));
            return cb.notEqual(path, argument);
        } else if (RSQLOperators.GREATER_THAN.equals(operator)) {
            Comparable argument = valueConverter.convertComparable(getValue(path, arguments.get(0)));
            return cb.greaterThan(path, argument);
        } else if (RSQLOperators.GREATER_THAN_OR_EQUAL.equals(operator)) {
            Comparable argument = valueConverter.convertComparable(getValue(path, arguments.get(0)));
            return cb.greaterThanOrEqualTo(path, argument);
        } else if (RSQLOperators.LESS_THAN.equals(operator)) {
            Comparable argument = valueConverter.convertComparable(getValue(path, arguments.get(0)));
            return cb.lessThan(path, argument);
        } else if (RSQLOperators.LESS_THAN_OR_EQUAL.equals(operator)) {
            Comparable argument = valueConverter.convertComparable(getValue(path, arguments.get(0)));
            return cb.lessThanOrEqualTo(path, argument);
        } else if (RSQLOperators.IN.equals(operator)) {
            List<Object> castArguments = castArguments(arguments, path);
            return path.in(castArguments);
        } else if (RSQLOperators.NOT_IN.equals(operator)) {
            List<Object> castArguments = castArguments(arguments, path);
            return cb.not(path.in(castArguments));
        } else if (LIKE.equals(operator)) {
            return cb.like(cb.lower(path), preprocessLikeOperatorArgument(arguments.get(0).toLowerCase()), ESCAPE_CHAR);
        } else if (NOT_LIKE.equals(operator)) {
            return cb.not(
                    cb.like(
                            cb.lower(path),
                            preprocessLikeOperatorArgument(arguments.get(0).toLowerCase()),
                            ESCAPE_CHAR));
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

    private Value getValue(Path path, String argument) {
        SingularAttribute pathAttribute = ((SingularAttributePath) path).getAttribute();
        Class<?> entityClass = pathAttribute.getJavaMember().getDeclaringClass();
        return new Value(entityClass, pathAttribute.getName(), path.getJavaType(), argument);
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
        return argument.replaceAll("_", Matcher.quoteReplacement(ESCAPE_CHAR + "_"))
                .replaceAll("\\" + WILDCARD_SINGLE_CHARACTER, "_")
                .replaceAll("\\" + WILDCARD_MULTIPLE_CHARACTERS, "%");
    }

    private List<Object> castArguments(List<String> arguments, Path path) {
        Stream<Object> objectStream = arguments.stream().map(a -> valueConverter.convert(getValue(path, a)));
        return objectStream.collect(Collectors.toList());
    }

}
