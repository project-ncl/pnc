package org.jboss.pnc.datastore.predicates;

import com.mysema.query.types.expr.BooleanExpression;
import org.jboss.pnc.model.QBuildConfiguration;

import static org.jboss.pnc.datastore.predicates.Utils.createNotNullPredicate;

public class BuildConfigurationPredicates {

    public static BooleanExpression withConfigurationId(Integer buildConfigurationId) {
        return createNotNullPredicate(buildConfigurationId != null, () -> QBuildConfiguration.buildConfiguration.id.eq(buildConfigurationId));
    }

    public static BooleanExpression withProjectId(Integer projectId) {
        return createNotNullPredicate(projectId != null, () -> QBuildConfiguration.buildConfiguration.project.id.eq(projectId));
    }

    public static BooleanExpression withProductId(Integer productId) {
        return createNotNullPredicate(productId != null, () -> QBuildConfiguration.buildConfiguration.productVersion.product.id.eq(productId));
    }

    public static BooleanExpression withProductVersionId(Integer productVersionId) {
        return createNotNullPredicate(productVersionId != null, () -> QBuildConfiguration.buildConfiguration.productVersion.id.eq(productVersionId));
    }

}
