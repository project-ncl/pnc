package org.jboss.pnc.datastore.predicates;

import com.mysema.query.types.expr.BooleanExpression;
import org.jboss.pnc.model.QProductMilestone;

import static org.jboss.pnc.datastore.predicates.Utils.createNotNullPredicate;

public class ProductMilestonePredicates {

    public static BooleanExpression withProductVersionId(Integer productVersionId) {
        return createNotNullPredicate(productVersionId != null, () -> QProductMilestone.productMilestone.productVersion.id.eq(productVersionId));
    }
}
