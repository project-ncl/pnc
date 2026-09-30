/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.repositorymanager;

import java.util.List;

import org.jboss.pnc.enums.BuildType;

public interface BuildExecution {

    String getId();

    String getBuildContentId();

    boolean isTempBuild();

    boolean isBrewPullActive();

    BuildType getBuildType();

    @Deprecated
    String getTempBuildTimestamp();

    /**
     * Gets the list of repositories needed to run a successful build.
     *
     * @return the list of artifact repositories
     */
    List<ArtifactRepository> getArtifactRepositories();

}
