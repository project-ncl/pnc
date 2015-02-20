package org.jboss.pnc.datastore.predicates;

import com.mysema.query.types.expr.BooleanExpression;
import org.jboss.pnc.model.QBuildRecord;

import static org.jboss.pnc.datastore.predicates.Utils.createNotNullPredicate;

public class BuildRecordRepositoryPredicates {

    public static BooleanExpression withBuildRecordId(Integer buildRecordId) {
        return createNotNullPredicate(buildRecordId != null, () -> QBuildRecord.buildRecord.id.eq(buildRecordId));
    }

    public static BooleanExpression withBuildConfigurationId(Integer configurationId) {
        return createNotNullPredicate(configurationId != null, () -> QBuildRecord.buildRecord.buildConfiguration.id.eq(configurationId));
    }
    
    public static BooleanExpression withProjectId(Integer projectId) {
        return createNotNullPredicate(projectId != null, () -> QBuildRecord.buildRecord.buildConfiguration.project.id.eq(projectId));
    }
}
