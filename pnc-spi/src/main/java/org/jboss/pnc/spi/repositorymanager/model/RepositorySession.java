package org.jboss.pnc.spi.repositorymanager.model;

import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public interface RepositorySession {

    RepositoryType getType();

    String getId();

    RepositoryConnectionInfo getConnectionInfo();

    /**
     * Promote any deployed artifacts and process any uncaptured imports of input artifacts (dependencies, etc.)
     *
     * @throws org.jboss.pnc.spi.repositorymanager.RepositoryManagerException
     */
    RepositoryManagerResult extractBuildArtifacts() throws RepositoryManagerException;
}
