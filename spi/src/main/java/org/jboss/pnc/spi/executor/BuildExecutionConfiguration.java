/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.executor;

import java.util.Map;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.api.enums.RebuildMode;
import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.spi.repositorymanager.BuildExecution;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public interface BuildExecutionConfiguration extends BuildExecution {

    @Override
    String getId();

    String getUserId();

    String getBuildScript();

    String getBuildConfigurationId();

    String getName(); // used to be buildConfiguration.name

    String getScmRepoURL();

    String getScmRevision();

    String getScmTag();

    String getScmBuildConfigRevision();

    Boolean isScmBuildConfigRevisionInternal();

    String getOriginRepoURL();

    boolean isPreBuildSyncEnabled();

    String getSystemImageId();

    String getSystemImageRepositoryUrl();

    SystemImageType getSystemImageType();

    boolean isPodKeptOnFailure();

    Map<String, String> getGenericParameters();

    String getDefaultAlignmentParams();

    AlignmentPreference getAlignmentPreference();

    RebuildMode getRebuildMode();
}
