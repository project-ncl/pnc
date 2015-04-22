package org.jboss.pnc.datastore.predicates;

import com.mysema.query.types.expr.BooleanExpression;
import org.jboss.pnc.model.ArtifactStatus;
import org.jboss.pnc.model.QArtifact;

import static org.jboss.pnc.datastore.predicates.Utils.createNotNullPredicate;

public class ArtifactPredicates {

    public static BooleanExpression withBuildRecordId(Integer buildRecordId) {
        return createNotNullPredicate(buildRecordId != null, () -> QArtifact.artifact.buildRecord.id.eq(buildRecordId));
    }

    public static BooleanExpression imported() {
        return QArtifact.artifact.status.in(ArtifactStatus.BINARY_IMPORTED);
    }

    public static BooleanExpression built() {
        return QArtifact.artifact.status.in(ArtifactStatus.BINARY_BUILT);
    }

    public static BooleanExpression withBuildRecordSetId(Integer buildRecordSetId) {
        return createNotNullPredicate(buildRecordSetId != null, () -> QArtifact.artifact.buildRecord.buildRecordSets.any().id.eq(buildRecordSetId));
    }

}
