package org.jboss.pnc.spi.repositorymanager;

import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.BuildExecution;
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
     * @param buildExecution
     */
    RepositorySession createBuildRepository(BuildExecution buildExecution)
            throws RepositoryManagerException;

    /**
     * Add the repository containing output associated with the specified {@link BuildRecord} to the membership of the
     * repository group with the given ID.
     * 
     * @param buildRecord The build output to promote
     * @param toGroup The ID of the repository group where the build output should be promoted
     * 
     * @return An object representing the running promotion process, with a callback method for the result.
     *
     * @throws RepositoryManagerException
     */
    RunningRepositoryPromotion promoteBuild(BuildRecord buildRecord, String toGroup)
            throws RepositoryManagerException;

    /**
     * Add the repository group containing output associated with the specified {@link BuildRecordSet} to the membership of the
     * repository group with the given ID.
     * 
     * @param buildRecordSet The record-set that should be promoted
     * @param toGroup The group into which the record-set should be promoted
     * 
     * @return An object representing the running promotion process, with a callback method for the result.
     * 
     * @throws RepositoryManagerException
     */
    RunningRepositoryPromotion promoteBuildSet(BuildRecordSet buildRecordSet, String toGroup)
            throws RepositoryManagerException;

    boolean canManage(RepositoryType managerType);

}
