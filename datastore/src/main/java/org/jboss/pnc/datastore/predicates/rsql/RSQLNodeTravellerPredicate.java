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
package org.jboss.pnc.datastore.predicates.rsql;

import com.google.common.base.Preconditions;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.RSQLParserException;
import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.EqualNode;
import cz.jirutka.rsql.parser.ast.GreaterThanNode;
import cz.jirutka.rsql.parser.ast.GreaterThanOrEqualNode;
import cz.jirutka.rsql.parser.ast.InNode;
import cz.jirutka.rsql.parser.ast.LessThanNode;
import cz.jirutka.rsql.parser.ast.LessThanOrEqualNode;
import cz.jirutka.rsql.parser.ast.LogicalNode;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.NotEqualNode;
import cz.jirutka.rsql.parser.ast.NotInNode;
import cz.jirutka.rsql.parser.ast.OrNode;
import org.jboss.pnc.model.GenericEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.*;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.jboss.pnc.datastore.predicates.rsql.AbstractTransformer.selectWithOperand;

public class RSQLNodeTravellerPredicate<Entity extends GenericEntity<? extends Number>> implements org.jboss.pnc.spi.datastore.repositories.api.Predicate<Entity> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Node rootNode;

    private final Class<Entity> selectingClass;

    private final Map<Class<? extends ComparisonNode>, Transformer<Entity>> operations = new HashMap<>();

    public RSQLNodeTravellerPredicate(Class<Entity> entityClass, String rsql) throws RSQLParserException {
        operations.put(EqualNode.class, new AbstractTransformer<Entity>() {
            @Override
            Predicate transform(Root<Entity> r, Path<?> selectedPath, CriteriaBuilder cb, String operand, List<Object> convertedArguments) {
                return cb.equal(selectedPath, convertedArguments.get(0));
            }
        });

        operations.put(NotEqualNode.class, new AbstractTransformer<Entity>() {
            @Override
            Predicate transform(Root<Entity> r, Path<?> selectedPath, CriteriaBuilder cb, String operand, List<Object> convertedArguments) {
                return cb.notEqual(selectedPath, convertedArguments.get(0));
            }
        });

        operations.put(GreaterThanNode.class, (r, cb, clazz, operand, arguments) -> cb.greaterThan((Path) selectWithOperand(r, operand), arguments.get(0)));
        operations.put(GreaterThanOrEqualNode.class, (r, cb, clazz, operand, arguments) -> cb.greaterThanOrEqualTo((Path)selectWithOperand(r, operand), arguments.get(0)));
        operations.put(LessThanNode.class, (r, cb, clazz, operand, arguments) -> cb.lessThan((Path)selectWithOperand(r, operand), arguments.get(0)));
        operations.put(LessThanOrEqualNode.class, (r, cb, clazz, operand, arguments) -> cb.lessThanOrEqualTo((Path)selectWithOperand(r, operand), arguments.get(0)));
        operations.put(InNode.class, (r, cb, clazz, operand, arguments) -> ((Path) selectWithOperand(r, operand)).in(arguments));
        operations.put(NotInNode.class, (r, cb, clazz, operand, arguments) -> cb.not((Path)selectWithOperand(r, operand)).in(arguments));
        operations.put(LikeNode.class, (r, cb, clazz, operand, arguments) -> cb.like(cb.lower((Path)selectWithOperand(r, operand)), arguments.get(0).toLowerCase()));

        rootNode = new RSQLParser(new ExtendedRSQLNodesFactory()).parse(rsql);
        selectingClass = entityClass;
    }

    @Override
    public javax.persistence.criteria.Predicate apply(Root root, CriteriaQuery query, CriteriaBuilder cb) {

        RSQLNodeTraveller<Predicate> visitor = new RSQLNodeTraveller<Predicate>() {

            public Predicate visit(LogicalNode node) {
                logger.info("Parsing LogicalNode {}", node);
                return proceedEmbeddedNodes(node);
            }

            public Predicate visit(ComparisonNode node) {
                logger.info("Parsing ComparisonNode {}", node);
                return proceedSelection(node);
            }

            private Predicate proceedSelection(ComparisonNode node) {
                Transformer<Entity> transformation = operations.get(node.getClass());
                Preconditions.checkArgument(transformation != null, "Operation not supported");

                return transformation.transform(root, cb, selectingClass, node.getSelector(), node.getArguments());
            }

            private Predicate proceedEmbeddedNodes(LogicalNode node) {
                Iterator<Node> iterator = node.iterator();
                if (node instanceof AndNode) {
                    return cb.and(visit(iterator.next()), visit(iterator.next()));
                } else if (node instanceof OrNode) {
                    return cb.or(visit(iterator.next()), visit(iterator.next()));
                } else {
                    throw new UnsupportedOperationException("Logical operation not supported");
                }
            }
        };

        return rootNode.accept(visitor);
    }
}
