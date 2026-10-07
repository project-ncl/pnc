/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.repositorymanager.model;

import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public interface RepositorySession {

    RepositoryType getType();

    String getBuildRepositoryId();

    RepositoryConnectionInfo getConnectionInfo();

    /**
     * Process any uncaptured imports of input artifacts (dependencies, etc.) and return the result containing
     * dependencies and build output.
     *
     * @param liveBuild flag if the build is live, i.e. if post-build actions should be performed
     * @return The result of extracting the build artifacts
     * @throws RepositoryManagerException if there is a problem extracting build artifacts
     */
    RepositoryManagerResult extractBuildArtifacts(boolean liveBuild) throws RepositoryManagerException;

    /**
     * Removes build aggregation group. This should be done for every build.
     *
     * @throws RepositoryManagerException in case of an error received from repository manager
     */
    void deleteBuildGroup() throws RepositoryManagerException;

    void close();
}
