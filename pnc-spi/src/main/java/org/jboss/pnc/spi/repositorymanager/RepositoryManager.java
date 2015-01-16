package org.jboss.pnc.spi.repositorymanager;

import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.RepositoryType;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public interface RepositoryManager {

    /**
     * Create a new repository configuration tuned to the parameters of that build collection and the build that will use this repository configuration.
     * 
     * @param buildConfiguration Used to name the repository configuration, and also for any build-specific variation from the 
     * product-default config values (which are keyed to the {@link BuildCollection}).
     * 
     * @param buildCollection Used to determine which in-progress product repository should be used.
     */
    public RepositoryConfiguration createRepository(BuildConfiguration buildConfiguration, BuildCollection buildCollection) throws RepositoryManagerException;

    boolean canManage(RepositoryType managerType);

    /**
     * Promote any deployed artifacts and process any uncaptured imports of input artifacts (dependencies, etc.)
     * @param repository Used during the build, containing input and output artifacts
     * @param buildRecord The record of the build, to which records of deployed / input artifacts should be attached
     */
    void persistArtifacts( RepositoryConfiguration repository, BuildRecord buildRecord );

}
