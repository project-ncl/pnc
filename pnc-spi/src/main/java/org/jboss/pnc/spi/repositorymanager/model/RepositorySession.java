package org.jboss.pnc.spi.repositorymanager.model;

import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public interface RepositorySession {

    RepositoryType getType();

    String getBuildRepositoryId();

    String getBuildSetRepositoryId();

    RepositoryConnectionInfo getConnectionInfo();

    /**
     * Process any uncaptured imports of input artifacts (dependencies, etc.) and return the result containing dependencies and
     * build output.
     *
     * @throws org.jboss.pnc.spi.repositorymanager.RepositoryManagerException
     */
    RepositoryManagerResult extractBuildArtifacts() throws RepositoryManagerException;

    /**
     * Promote the build repository containing build output to the content-set group, if it exists.
     */
    void promoteToBuildContentSet() throws RepositoryManagerException;
}
