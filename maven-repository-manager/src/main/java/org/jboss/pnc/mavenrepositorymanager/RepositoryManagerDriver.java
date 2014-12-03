package org.jboss.pnc.mavenrepositorymanager;

import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.ProjectBuildResult;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;

/**
 * Implementation of {@link RepositoryManager} that manages an <a href="https://github.com/jdcasey/aprox">AProx</a> instance to
 * support repositories for Maven-ish builds.
 * 
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-25.
 */
public class RepositoryManagerDriver implements RepositoryManager {

    /**
     * Only supports {@link RepositoryType#MAVEN}.
     */
    @Override
    public boolean canManage(RepositoryType managerType) {
        return (managerType == RepositoryType.MAVEN);
    }

    /**
     * Currently, we're using the AutoProx add-on of AProx. This add-on supports a series of rules (implemented as groovy
     * scripts), which match repository/group naming patterns and create remote repositories, hosted repositories, and groups in
     * flexible configurations on demand. Because these rules will create the necessary repositories the first time they are
     * accessed, this driver only has to formulate a repository URL that will trigger the appropriate AutoProx rule, then pass
     * this back via a {@link MavenRepositoryConfiguration} instance.
     */
    @Override
    public RepositoryConfiguration createRepository(ProjectBuildConfiguration projectBuildConfiguration,
            BuildCollection buildCollection) {
        // TODO Better way to generate id.
        String id = String.format("build+%s+%s+%s+%s", buildCollection.getProductVersion().getProduct().getName(),
                buildCollection.getProductVersion().getVersion(),
                safeUrlPart(projectBuildConfiguration.getProject().getName()), System.currentTimeMillis());
        return new MavenRepositoryConfiguration(id, new MavenRepositoryConnectionInfo());
    }

    @Override
    public void persistArtifacts(RepositoryConfiguration repository, ProjectBuildResult buildResult) {
        // TODO Listing/sifting of imports, promotion of output artifacts to build result
    }

    /**
     * Sift out spaces, pipe characters and colons (things that don't play well in URLs) from the project name, and convert them
     * to dashes. This is only for naming repositories, so an approximate match to the project in question is fine.
     */
    private String safeUrlPart(String name) {
        return name.replaceAll("\\W+", "-").replaceAll("[|:]+", "-");
    }

}
