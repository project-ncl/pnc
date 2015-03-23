package org.jboss.pnc.spi.repositorymanager;

import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConfiguration;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public interface RepositoryManager {

    /**
     * Create a new repository configuration tuned to the parameters of that build collection and the build that will use this
     * repository configuration.
     * 
     * @param buildConfiguration Used to name the repository configuration, and also for any build-specific variation from the
     *        product-default config values (which are keyed to the {@link BuildRecordSet}).
     * 
     * @param buildRecordSet Used to determine which in-progress product repository should be used.
     * @throws RepositoryManagerException
     */
    RepositoryConfiguration createRepository(BuildConfiguration buildConfiguration, BuildRecordSet buildRecordSet)
            throws RepositoryManagerException;

    boolean canManage(RepositoryType managerType);

}
