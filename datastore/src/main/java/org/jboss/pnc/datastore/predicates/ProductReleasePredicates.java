package org.jboss.pnc.datastore.predicates;

import com.mysema.query.types.expr.BooleanExpression;
import org.jboss.pnc.model.QProductRelease;

import static org.jboss.pnc.datastore.predicates.Utils.createNotNullPredicate;

public class ProductReleasePredicates {

    public static BooleanExpression withProductVersionId(Integer productVersionId) {
        return createNotNullPredicate(productVersionId != null, () -> QProductRelease.productRelease.productVersion.id.eq(productVersionId));
    }
}
