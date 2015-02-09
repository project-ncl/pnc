package org.jboss.pnc.datastore.predicates;

import com.mysema.query.types.expr.BooleanExpression;
import org.jboss.pnc.model.QArtifact;

import static org.jboss.pnc.datastore.predicates.Utils.createNotNullPredicate;

public class ArtifactPredicates {

    public static BooleanExpression withBuildRecordId(Integer buildRecordId) {
        return createNotNullPredicate(buildRecordId != null, () -> QArtifact.artifact.buildRecord.id.eq(buildRecordId));
    }

}
