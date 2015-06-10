package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.Project;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.Project} entity.
 */
public interface ProjectRepository extends Repository<Project, Integer> {
}
