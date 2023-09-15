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

package org.jboss.pnc.bpm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.dto.User;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.repositorymanager.ArtifactRepository;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Data
@AllArgsConstructor
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
@JsonDeserialize(builder = BuildExecutionConfigurationRest.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement(name = "buildExecutionConfiguration")
public class BuildExecutionConfigurationRest {

    protected String id;
    protected String buildContentId;
    protected User user;
    protected String buildScript;
    protected String buildConfigurationId;
    protected String name;

    protected String scmRepoURL;
    protected String scmRevision;
    protected String scmTag;
    protected String scmBuildConfigRevision;
    protected boolean scmBuildConfigRevisionInternal;
    protected String originRepoURL;
    protected boolean preBuildSyncEnabled;

    protected BuildType buildType;
    protected String systemImageId;
    protected String systemImageRepositoryUrl;
    protected SystemImageType systemImageType;
    protected boolean podKeptOnFailure = false;
    protected List<ArtifactRepositoryRest> artifactRepositories;
    protected Map<String, String> genericParameters;

    protected boolean tempBuild;

    @Deprecated
    protected String tempBuildTimestamp;

    protected boolean brewPullActive;

    protected String defaultAlignmentParams;

    protected AlignmentPreference alignmentPreference;

    public static BuildExecutionConfigurationRest valueOf(String serialized) throws IOException {
        TypeReference<BuildExecutionConfigurationRest> type = new TypeReference<>() {
        };
        return JsonOutputConverterMapper.getMapper().readValue(serialized, type);
    }

    public BuildExecutionConfigurationRest(BuildExecutionConfiguration buildExecutionConfiguration) {
        id = buildExecutionConfiguration.getId();
        buildContentId = buildExecutionConfiguration.getBuildContentId();
        buildScript = buildExecutionConfiguration.getBuildScript();
        buildConfigurationId = buildExecutionConfiguration.getBuildConfigurationId();
        name = buildExecutionConfiguration.getName();
        scmRepoURL = buildExecutionConfiguration.getScmRepoURL();
        scmRevision = buildExecutionConfiguration.getScmRevision();
        scmTag = buildExecutionConfiguration.getScmTag();
        scmBuildConfigRevision = buildExecutionConfiguration.getScmBuildConfigRevision();
        scmBuildConfigRevisionInternal = buildExecutionConfiguration.isScmBuildConfigRevisionInternal();
        originRepoURL = buildExecutionConfiguration.getOriginRepoURL();
        preBuildSyncEnabled = buildExecutionConfiguration.isPreBuildSyncEnabled();
        buildType = buildExecutionConfiguration.getBuildType();
        systemImageId = buildExecutionConfiguration.getSystemImageId();
        systemImageRepositoryUrl = buildExecutionConfiguration.getSystemImageRepositoryUrl();
        systemImageType = buildExecutionConfiguration.getSystemImageType();
        user = User.builder().id(buildExecutionConfiguration.getUserId()).build();
        podKeptOnFailure = buildExecutionConfiguration.isPodKeptOnFailure();
        genericParameters = buildExecutionConfiguration.getGenericParameters();
        tempBuild = buildExecutionConfiguration.isTempBuild();
        tempBuildTimestamp = buildExecutionConfiguration.getTempBuildTimestamp();
        brewPullActive = buildExecutionConfiguration.isBrewPullActive();
        defaultAlignmentParams = buildExecutionConfiguration.getDefaultAlignmentParams();
        alignmentPreference = buildExecutionConfiguration.getAlignmentPreference();

        artifactRepositories = new ArrayList<>();
        if (buildExecutionConfiguration.getArtifactRepositories() != null) {
            for (ArtifactRepository artifactRepository : buildExecutionConfiguration.getArtifactRepositories()) {
                artifactRepositories.add(new ArtifactRepositoryRest(artifactRepository));
            }
        }
    }

    public BuildExecutionConfiguration toBuildExecutionConfiguration() {
        List<ArtifactRepository> artifactRepositories = this.artifactRepositories.stream()
                .map(ArtifactRepositoryRest::toArtifactRepository)
                .collect(Collectors.toList());
        return BuildExecutionConfiguration.build(
                id,
                buildContentId,
                user.getId(),
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
                systemImageId,
                systemImageRepositoryUrl,
                systemImageType,
                buildType,
                podKeptOnFailure,
                artifactRepositories,
                genericParameters,
                tempBuild,
                tempBuildTimestamp,
                brewPullActive,
                defaultAlignmentParams,
                alignmentPreference);
    }

    @JsonIgnore
    public String getUserId() {
        return user.getId();
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }

    @Override
    public String toString() {
        return JsonOutputConverterMapper.apply(this);
    }

}
