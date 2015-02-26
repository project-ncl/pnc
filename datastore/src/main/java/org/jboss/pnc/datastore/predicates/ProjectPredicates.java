package org.jboss.pnc.datastore.predicates;

import com.mysema.query.types.expr.BooleanExpression;
import org.jboss.pnc.model.QProject;

import static org.jboss.pnc.datastore.predicates.Utils.createNotNullPredicate;

public class ProjectPredicates {

    public static BooleanExpression withProjectId(Integer projectId) {
        return createNotNullPredicate(projectId != null, () -> QProject.project.id.eq(projectId));
    }

}
