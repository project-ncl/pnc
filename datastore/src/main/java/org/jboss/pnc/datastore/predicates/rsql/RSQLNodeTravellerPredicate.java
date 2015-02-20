package org.jboss.pnc.datastore.predicates.rsql;

import java.beans.Introspector;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jboss.pnc.datastore.predicates.RSQLPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.path.PathBuilder;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.RSQLParserException;
import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.EqualNode;
import cz.jirutka.rsql.parser.ast.InNode;
import cz.jirutka.rsql.parser.ast.LogicalNode;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.NotEqualNode;
import cz.jirutka.rsql.parser.ast.NotInNode;
import cz.jirutka.rsql.parser.ast.OrNode;

public class RSQLNodeTravellerPredicate<Entity> implements RSQLPredicate {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Node rootNode;

    private final Class<Entity> selectingClass;

    private final Map<Class<? extends ComparisonNode>, Transformer<Entity>> operations = new HashMap<>();

    public RSQLNodeTravellerPredicate(Class<Entity> entityClass, String rsql) throws RSQLParserException {

        operations.put(EqualNode.class, (pathBuilder, operand, arguments) -> pathBuilder.get(operand).eq(arguments.get(0)));
        operations.put(NotEqualNode.class, (pathBuilder, operand, arguments) -> pathBuilder.get(operand).ne(arguments.get(0)));
        operations.put(InNode.class, (pathBuilder, operand, arguments) -> pathBuilder.get(operand).in(arguments));
        operations.put(NotInNode.class, (pathBuilder, operand, arguments) -> pathBuilder.get(operand).notIn(arguments));

        this.rootNode = new RSQLParser().parse(rsql);
        this.selectingClass = entityClass;
    }

    @Override
    public BooleanExpression get() {

        // Using lower-cases string variables makes Entities with camelCase names unusable (i.e. BuildRecord)
        PathBuilder<Entity> pathBuilder = new PathBuilder<>(selectingClass, Introspector.decapitalize(selectingClass
                .getSimpleName()));

        RSQLNodeTraveller<BooleanExpression> visitor = new RSQLNodeTraveller<BooleanExpression>() {

            public BooleanExpression visit(LogicalNode node) {
                logger.info("Parsing LogicalNode {}", node);
                return proceedEmbeddedNodes(node);
            }

            public BooleanExpression visit(ComparisonNode node) {
                logger.info("Parsing ComparisonNode {}", node);
                return proceedSelection(node);
            }

            private BooleanExpression proceedSelection(ComparisonNode node) {
                Transformer<Entity> transformation = operations.get(node.getClass());
                Preconditions.checkArgument(transformation != null, "Operation not supported");
                BooleanExpression expression = transformation.transform(pathBuilder, node.getSelector(), node.getArguments());
                return expression;
            }

            private BooleanExpression proceedEmbeddedNodes(LogicalNode node) {
                Iterator<Node> iterator = node.iterator();
                if (node instanceof AndNode) {
                    return visit(iterator.next()).and(visit(iterator.next()));
                } else if (node instanceof OrNode) {
                    return visit(iterator.next()).or(visit(iterator.next()));
                } else {
                    throw new UnsupportedOperationException("Logical operation not supported");
                }
            }
        };

        return rootNode.accept(visitor);
    }

}
