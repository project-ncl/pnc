package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.datastore.repositories.internal.ProjectSpringRepository;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.spi.datastore.repositories.ProjectRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class ProjectRepositoryImpl extends AbstractRepository<Project, Integer> implements ProjectRepository {

    /**
     * @deprecated Created for CDI.
     */
    @Deprecated
    public ProjectRepositoryImpl() {
        super(null, null);
    }

    @Inject
    public ProjectRepositoryImpl(ProjectSpringRepository projectSpringRepository) {
        super(projectSpringRepository, projectSpringRepository);
    }
}
