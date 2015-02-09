package org.jboss.pnc.datastore;

import com.mysema.query.types.expr.BooleanExpression;
import org.jboss.pnc.model.QProductVersion;

import static org.jboss.pnc.datastore.predicates.Utils.createNotNullPredicate;

public class ProductVersionPredicates {

    public static BooleanExpression withProductId(Integer productId) {
        return createNotNullPredicate(productId != null, () -> QProductVersion.productVersion.product.id.eq(productId));
    }

    public static BooleanExpression withProductVersionId(Integer productVersionId) {
        return createNotNullPredicate(productVersionId != null, () -> QProductVersion.productVersion.id.eq(productVersionId));
    }
}
