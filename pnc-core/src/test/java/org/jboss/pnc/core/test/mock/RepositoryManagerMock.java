package org.jboss.pnc.core.test.mock;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.core.repository.RepositoryConfigurationImpl;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.model.RepositoryManagerType;

import java.util.List;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-25.
 */
public class RepositoryManagerMock implements RepositoryManager {
    
    @Override
    public boolean canManage(RepositoryManagerType managerType) {
        return true;
    }

    @Override
    public RepositoryConfigurationImpl createBuildRepository(BuildConfiguration b)
    {
        return new RepositoryConfigurationImpl();
    }

    @Override
    public List<Artifact> promoteBuildRepositoryOutput(RepositoryConfiguration b) {
        return null;
    }

    @Override
    public List<Artifact> getBuildRepositoryInput(RepositoryConfiguration b) {
        return null;
    }

    @Override
    public void closeBuildRepository(RepositoryConfiguration b) {

    }
}
