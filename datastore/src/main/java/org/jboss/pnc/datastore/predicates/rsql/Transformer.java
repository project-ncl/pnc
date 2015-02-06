package org.jboss.pnc.datastore.predicates.rsql;

import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.path.PathBuilder;

import java.util.List;

@FunctionalInterface
interface Transformer<Entity> {
    BooleanExpression transform(PathBuilder<Entity> pathBuilder, String operand, List<String> arguments);
}
