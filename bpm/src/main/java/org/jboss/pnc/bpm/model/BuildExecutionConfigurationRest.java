/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = BuildExecutionConfigurationRest.Builder.class)
@XmlRootElement(name = "buildExecutionConfiguration")
public class BuildExecutionConfigurationRest implements BuildExecutionConfiguration {

    private int id;
    private String buildContentId;
    private User user;
    private String buildScript;
    private String name;

    private String scmRepoURL;
    private String scmRevision;
    private String scmTag;
    private String originRepoURL;
    private boolean preBuildSyncEnabled;

    private BuildType buildType;
    private String systemImageId;
    private String systemImageRepositoryUrl;
    private SystemImageType systemImageType;
    private boolean podKeptOnFailure = false;
    private List<ArtifactRepository> artifactRepositories;
    private Map<String, String> genericParameters;

    private boolean tempBuild;

    private String tempBuildTimestamp;

    public static BuildExecutionConfigurationRest valueOf(String serialized) throws IOException {
        return JsonOutputConverterMapper.readValue(serialized, BuildExecutionConfigurationRest.class);
    }

    public BuildExecutionConfigurationRest(BuildExecutionConfiguration buildExecutionConfiguration) {
        id = buildExecutionConfiguration.getId();
        buildContentId = buildExecutionConfiguration.getBuildContentId();
        buildScript = buildExecutionConfiguration.getBuildScript();
        name = buildExecutionConfiguration.getName();
        scmRepoURL = buildExecutionConfiguration.getScmRepoURL();
        scmRevision = buildExecutionConfiguration.getScmRevision();
        scmTag = buildExecutionConfiguration.getScmTag();
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

        artifactRepositories = new ArrayList<>();
        if (buildExecutionConfiguration.getArtifactRepositories() != null) {
            for (ArtifactRepository artifactRepository : buildExecutionConfiguration.getArtifactRepositories()) {
                artifactRepositories.add(new ArtifactRepositoryRest(artifactRepository));
            }
        }
    }

    public BuildExecutionConfiguration toBuildExecutionConfiguration() {
        return BuildExecutionConfiguration.build(
                id,
                buildContentId,
                user.getId(),
                buildScript,
                name,
                scmRepoURL,
                scmRevision,
                scmTag,
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
                tempBuildTimestamp);
    }

    @JsonIgnore
    @Override
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
