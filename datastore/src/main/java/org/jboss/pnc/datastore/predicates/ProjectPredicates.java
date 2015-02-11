package org.jboss.pnc.datastore.predicates;

import com.mysema.query.types.expr.BooleanExpression;
import org.jboss.pnc.model.QProject;

import static org.jboss.pnc.datastore.predicates.Utils.createNotNullPredicate;

public class ProjectPredicates {

    public static BooleanExpression withProjectId(Integer projectId) {
        return createNotNullPredicate(projectId != null, () -> QProject.project.id.eq(projectId));
    }

    public static BooleanExpression withProductVersionId(Integer productVersionId) {
        return createNotNullPredicate(productVersionId != null,
                () -> QProject.project.productVersionProjects.any().productVersion.id.eq(productVersionId));
    }

    public static BooleanExpression withProductId(Integer productId) {
        return createNotNullPredicate(productId != null,
                () -> QProject.project.productVersionProjects.any().productVersion.product.id.eq(productId));
    }

}
