package org.jboss.pnc.spi.repositorymanager;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.spi.repositorymanager.model.RunningRepositoryPromotion;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public interface RepositoryManager {

    /**
     * Create a new repository session tuned to the parameters of that build collection and the build that will use this
     * repository session.
     * 
     * @param buildConfiguration Used to name the repository configuration, and also for any build-specific variation from the
     *        product-default config values (which are keyed to the {@link BuildRecordSet}).
     * 
     * @param buildRecordSet Used to determine which in-progress product repository should be used.
     * @throws RepositoryManagerException
     */
    RepositorySession createBuildRepository(BuildConfiguration buildConfiguration, BuildRecordSet buildRecordSet)
            throws RepositoryManagerException;

    /**
     * Add the repository containing output associated with the specified {@link BuildRecord} to the membership of the
     * repository group associated with the specified {@link BuildRecordSet}.
     * 
     * @param buildRecord The build output to add to the record-set
     * @param buildRecordSet The record-set into which the output should be promoted
     * 
     * @return An object representing the running promotion process, with a callback method for the result.
     * 
     * @throws RepositoryManagerException
     */
    RunningRepositoryPromotion promoteRepository(BuildRecord buildRecord, BuildRecordSet buildRecordSet)
            throws RepositoryManagerException;

    boolean canManage(RepositoryType managerType);

}
