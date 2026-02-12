/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.pnc.dto.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.dto.User;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.enums.SystemImageType;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

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
                alignmentPreference);
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
            AlignmentPreference alignmentPreference) {
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
                alignmentPreference);
        this.completionCallbackUrl = completionCallbackUrl;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }

}
