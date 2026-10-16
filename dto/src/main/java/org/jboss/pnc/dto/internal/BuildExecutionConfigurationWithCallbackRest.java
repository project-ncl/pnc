/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.internal;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.api.enums.RebuildMode;
import org.jboss.pnc.dto.User;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.enums.SystemImageType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = BuildExecutionConfigurationWithCallbackRest.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildExecutionConfigurationWithCallbackRest extends BuildExecutionConfigurationRest
        implements Serializable {

    private String completionCallbackUrl;

    public BuildExecutionConfigurationWithCallbackRest(
            String id,
            String buildContentId,
            User user,
            String buildScript,
            String buildConfigurationId,
            String name,
            String scmRepoURL,
            String scmRevision,
            String scmTag,
            String scmBuildConfigRevision,
            Boolean scmBuildConfigRevisionInternal,
            String originRepoURL,
            boolean preBuildSyncEnabled,
            BuildType buildType,
            String systemImageId,
            String systemImageRepositoryUrl,
            SystemImageType systemImageType,
            boolean podKeptOnFailure,
            List<ArtifactRepositoryRest> artifactRepositories,
            Map<String, String> genericParameters,
            boolean tempBuild,
            String tempBuildTimestamp,
            boolean brewPullActive,
            String defaultAlignmentParams,
            AlignmentPreference alignmentPreference,
            RebuildMode rebuildMode,
            String completionCallbackUrl) {
        super(
                id,
                buildContentId,
                user,
                buildScript,
                buildConfigurationId,
                name,
                scmRepoURL,
                scmRevision,
                scmTag,
                scmBuildConfigRevision,
                scmBuildConfigRevisionInternal,
                originRepoURL,
                preBuildSyncEnabled,
                buildType,
                systemImageId,
                systemImageRepositoryUrl,
                systemImageType,
                podKeptOnFailure,
                artifactRepositories,
                genericParameters,
                tempBuild,
                tempBuildTimestamp,
                brewPullActive,
                defaultAlignmentParams,
                alignmentPreference,
                rebuildMode);
        this.completionCallbackUrl = completionCallbackUrl;
    }

    @lombok.Builder(builderClassName = "Builder")
    public BuildExecutionConfigurationWithCallbackRest(
            String id,
            String buildContentId,
            User user,
            String buildScript,
            String buildConfigurationId,
            String name,
            String scmRepoURL,
            String scmRevision,
            String scmTag,
            String scmBuildConfigRevision,
            boolean scmBuildConfigRevisionInternal,
            String originRepoURL,
            boolean preBuildSyncEnabled,
            BuildType buildType,
            String systemImageId,
            String systemImageRepositoryUrl,
            SystemImageType systemImageType,
            boolean podKeptOnFailure,
            List<ArtifactRepositoryRest> artifactRepositories,
            Map<String, String> genericParameters,
            boolean tempBuild,
            String tempBuildTimestamp,
            boolean brewPullActive,
            String completionCallbackUrl,
            String defaultAlignmentParams,
            AlignmentPreference alignmentPreference,
            RebuildMode rebuildMode) {
        super(
                id,
                buildContentId,
                user,
                buildScript,
                buildConfigurationId,
                name,
                scmRepoURL,
                scmRevision,
                scmTag,
                scmBuildConfigRevision,
                scmBuildConfigRevisionInternal,
                originRepoURL,
                preBuildSyncEnabled,
                buildType,
                systemImageId,
                systemImageRepositoryUrl,
                systemImageType,
                podKeptOnFailure,
                artifactRepositories,
                genericParameters,
                tempBuild,
                tempBuildTimestamp,
                brewPullActive,
                defaultAlignmentParams,
                alignmentPreference,
                rebuildMode);
        this.completionCallbackUrl = completionCallbackUrl;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }

}
