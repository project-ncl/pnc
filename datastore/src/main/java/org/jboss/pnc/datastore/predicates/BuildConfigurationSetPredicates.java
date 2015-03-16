package org.jboss.pnc.datastore.predicates;

import com.mysema.query.types.expr.BooleanExpression;
import org.jboss.pnc.model.QBuildConfigurationSet;

import static org.jboss.pnc.datastore.predicates.Utils.createNotNullPredicate;

public class BuildConfigurationSetPredicates {

    public static BooleanExpression withBuildConfigurationSetId(Integer configurationSetId) {
        return createNotNullPredicate(configurationSetId != null, () -> QBuildConfigurationSet.buildConfigurationSet.id.eq(configurationSetId));
    }
}
