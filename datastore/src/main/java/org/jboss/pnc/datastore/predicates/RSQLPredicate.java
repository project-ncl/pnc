package org.jboss.pnc.datastore.predicates;

import com.mysema.query.types.expr.BooleanExpression;

import java.util.function.Supplier;

/**
 * Performs a transformation between RSQL and JPA's Predicates.
 */
@FunctionalInterface
public interface RSQLPredicate extends Supplier<BooleanExpression> {

    /**
     * Creates new Predicate.
     *
     * @return A predicate for filtering DB results.
     */
    BooleanExpression get();
}
