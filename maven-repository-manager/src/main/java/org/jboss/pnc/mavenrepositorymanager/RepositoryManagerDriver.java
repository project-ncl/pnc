package org.jboss.pnc.mavenrepositorymanager;

import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.ProjectBuildResult;
import org.jboss.pnc.model.RepositoryManagerType;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;

import java.util.logging.Logger;

import javax.inject.Inject;

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
    public RepositoryConfiguration createRepository(ProjectBuildConfiguration projectBuildConfiguration,
            BuildCollection buildCollection) {
        // TODO Better way to generate id.
        String id = String.format("build+%s+%s+%s+%s", buildCollection.getProductName(), buildCollection.getProductVersion(),
                safeUrlPart(projectBuildConfiguration.getProject().getName()), System.currentTimeMillis());
        return new MavenRepositoryConfiguration(id, new MavenRepositoryConnectionInfo());
    }

    private String safeUrlPart(String name) {
        return name.replaceAll("\\W+", "-").replaceAll("[|:]+", "-");
    }

    @Override
    public void persistArtifacts(RepositoryConfiguration repository, ProjectBuildResult buildResult) {
        // TODO Listing/sifting of imports, promotion of output artifacts to build result
    }
}
