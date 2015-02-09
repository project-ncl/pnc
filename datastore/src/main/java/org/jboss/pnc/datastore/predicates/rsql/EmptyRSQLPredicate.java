package org.jboss.pnc.datastore.predicates.rsql;


import com.mysema.query.types.expr.BooleanExpression;
import org.jboss.pnc.datastore.predicates.RSQLPredicate;

/**
 * Empty implementation of a RSQL adapter
 *
 * <p>
 *     Converts RSQL query into Spring Data's {@link org.springframework.data.jpa.domain.Specification}, which in turn
 *     might be used for selecting records.
 * </p>
 */
public class EmptyRSQLPredicate implements RSQLPredicate {

   @Override
   public BooleanExpression get() {
      return null;
   }

}
