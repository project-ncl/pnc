package org.jboss.pnc.datastore.predicates.rsql;

import com.mysema.query.types.expr.BooleanExpression;

/**
 * Performs a transformation between RSQL and JPA's Predicates.
 */
@FunctionalInterface
public interface RSQLPredicate<Entity> {

    /**
     * Creates new Predicate.
     *
     * @return A predicate for filtering DB results.
     */
    BooleanExpression toPredicate();
}
