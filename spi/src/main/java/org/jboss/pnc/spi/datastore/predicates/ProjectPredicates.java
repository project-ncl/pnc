package org.jboss.pnc.spi.datastore.predicates;

import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.Project_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

/**
 * Predicates for {@link org.jboss.pnc.model.Project} entity.
 */
public class ProjectPredicates {

    public static Predicate<Project> withProjectId(Integer projectId) {
        return (root, query, cb) -> cb.equal(root.get(Project_.id), projectId);
    }

}
