package org.jboss.pnc.datastore;

import org.jboss.pnc.datastore.repositories.ProjectBuildResultRepository;
import org.jboss.pnc.model.ProjectBuildResult;
import org.jboss.pnc.spi.datastore.Datastore;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DefaultDatastore implements Datastore {

    @Inject
    ProjectBuildResultRepository projectBuildResultRepository;

    @Override
    public void storeCompletedBuild(ProjectBuildResult buildResult) {
        projectBuildResultRepository.save(buildResult);
    }
}
