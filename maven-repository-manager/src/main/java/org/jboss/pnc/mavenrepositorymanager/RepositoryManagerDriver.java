package org.jboss.pnc.mavenrepositorymanager;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.RepositoryManagerType;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;

import javax.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-25.
 */
public class RepositoryManagerDriver implements RepositoryManager {

    @Inject
    private Logger log;

    public RepositoryManagerDriver()
    {
    }

    @Override
    public boolean canManage(RepositoryManagerType managerType)
    {
        log.info("Checking for type " + managerType);
        if (managerType == RepositoryManagerType.MAVEN)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public MavenRepositoryConfiguration createBuildRepository(ProjectBuildConfiguration b)
    {
        return new MavenRepositoryConfiguration();
    }

    @Override
    public List<Artifact> promoteBuildRepositoryOutput(org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration b) {
        return null;
    }

    @Override
    public List<Artifact> getBuildRepositoryInput(org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration b) {
        return null;
    }

    @Override
    public void closeBuildRepository(org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration b) {

    }
}
