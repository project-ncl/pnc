package org.jboss.pnc.datastore.predicates;

import com.mysema.query.types.expr.BooleanExpression;

import org.jboss.pnc.model.QBuildRecordSet;

import static org.jboss.pnc.datastore.predicates.Utils.createNotNullPredicate;

public class BuildRecordSetPredicates {

    public static BooleanExpression withProductVersionId(Integer productVersionId) {
        return createNotNullPredicate(productVersionId != null, () -> QBuildRecordSet.buildRecordSet.productVersion.id.eq(productVersionId));
    }
    
    public static BooleanExpression withBuildRecordId(Integer buildRecordId) {
        return createNotNullPredicate(buildRecordId != null,
                () -> QBuildRecordSet.buildRecordSet.buildRecord.any().id.eq(buildRecordId));
    }
}
