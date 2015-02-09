package org.jboss.pnc.datastore.predicates;

import com.mysema.query.types.expr.BooleanExpression;

import java.util.function.Supplier;

public class Utils {

    public static BooleanExpression createNotNullPredicate(Boolean expression, Supplier<BooleanExpression> predicateSupplier) {
        if(expression == null) {
            return null;
        }
        return predicateSupplier.get();
    }

}
