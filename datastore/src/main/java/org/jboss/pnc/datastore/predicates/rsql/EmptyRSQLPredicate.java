package org.jboss.pnc.datastore.predicates.rsql;


import com.mysema.query.types.expr.BooleanExpression;

/**
 * Empty implementation of a RSQL adapter
 *
 * <p>
 *     Converts RSQL query into Spring Data's {@link org.springframework.data.jpa.domain.Specification}, which in turn
 *     might be used for selecting records.
 * </p>
 * @param <Entity> An entity type.
 */
public class EmptyRSQLPredicate<Entity> implements RSQLPredicate<Entity> {

   @Override
   public BooleanExpression toPredicate() {
      return null;
   }

}
