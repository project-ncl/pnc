package org.jboss.pnc.spi.datastore.predicates;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Artifact_;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

import javax.persistence.criteria.Join;

/**
 * Predicates for {@link org.jboss.pnc.model.Artifact} entity.
 */
public class ArtifactPredicates {

    public static Predicate<Artifact> withBuildRecordId(Integer buildRecordId) {
        return (root, query, cb) -> {
            Join<Artifact, BuildRecord> buildRecord = root.join(Artifact_.buildRecord);
            return cb.equal(buildRecord.get(BuildRecord_.id), buildRecordId);
        };
    }

}
