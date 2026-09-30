/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.executor;

import java.util.List;
import java.util.Map;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.api.enums.RebuildMode;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.spi.repositorymanager.ArtifactRepository;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DefaultBuildExecutionConfiguration implements BuildExecutionConfiguration {

    private final String id;

    private final String buildContentId;

    private final String userId;

    private final String buildScript;

    private final String buildConfigurationId;

    private final String name;

    private final String scmRepoURL;

    private final String scmRevision;

    private final String scmTag;

    private final String scmBuildConfigRevision;

    private final Boolean scmBuildConfigRevisionInternal;

    private final String originRepoURL;

    private final boolean preBuildSyncEnabled;

    private final String systemImageId;

    private final String systemImageRepositoryUrl;

    private final SystemImageType systemImageType;

    private final BuildType buildType;

    private final boolean podKeptOnFailure;

    private final List<ArtifactRepository> artifactRepositories;

    private final Map<String, String> genericParameters;

    private final boolean tempBuild;

    private final String tempBuildTimestamp;

    private final boolean brewPullActive;

    private final String defaultAlignmentParams;

    private final AlignmentPreference alignmentPreference;

    private final RebuildMode rebuildMode;

    @Override
    public Boolean isScmBuildConfigRevisionInternal() {
        return getScmBuildConfigRevisionInternal();
    }
}
