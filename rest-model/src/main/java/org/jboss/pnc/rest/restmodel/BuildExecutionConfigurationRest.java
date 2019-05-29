/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

package org.jboss.pnc.rest.restmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.SystemImageType;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
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
@XmlRootElement(name = "buildExecutionConfiguration")
public class BuildExecutionConfigurationRest implements BuildExecutionConfiguration {

    private int id;
    private String buildContentId;
    private UserRest user;
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

    @Getter
    @Setter
    private String tempBuildTimestamp;

    public BuildExecutionConfigurationRest() {}

    public BuildExecutionConfigurationRest(String serialized) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        BuildExecutionConfigurationRest buildExecutionConfigurationRestFromJson = mapper.readValue(serialized, BuildExecutionConfigurationRest.class);
        BuildExecutionConfiguration buildExecutionConfiguration = buildExecutionConfigurationRestFromJson.toBuildExecutionConfiguration();

        init(buildExecutionConfiguration);
    }

    public BuildExecutionConfigurationRest(BuildExecutionConfiguration buildExecutionConfiguration) {
        init(buildExecutionConfiguration);
    }

    private void init(BuildExecutionConfiguration buildExecutionConfiguration) {
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
        user = new UserRest(buildExecutionConfiguration.getUserId());
        podKeptOnFailure = buildExecutionConfiguration.isPodKeptOnFailure();
        genericParameters = buildExecutionConfiguration.getGenericParameters();
        tempBuild = buildExecutionConfiguration.isTempBuild();
        tempBuildTimestamp = buildExecutionConfiguration.getTempBuildTimestamp();

        if (buildExecutionConfiguration.getArtifactRepositories() != null) {
            artifactRepositories = new ArrayList<>(buildExecutionConfiguration.getArtifactRepositories().size());
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
                tempBuildTimestamp
        );
    }

    public BuildConfigurationAuditedRest createBuildConfigurationAuditedRest() {
        BuildConfigurationAuditedRest buildConfigAuditedRest = new BuildConfigurationAuditedRest();
        buildConfigAuditedRest.setId(id);
        buildConfigAuditedRest.setRev(null);
        buildConfigAuditedRest.setName(name);
        buildConfigAuditedRest.setBuildScript(buildScript);

        //TODO update to use also other parts or Repository Configuration
        RepositoryConfigurationRest repoConfigRest = new RepositoryConfigurationRest();
        repoConfigRest.setInternalUrl(scmRepoURL);
        buildConfigAuditedRest.setRepositoryConfiguration(repoConfigRest);
        buildConfigAuditedRest.setScmRevision(scmRevision);
        return buildConfigAuditedRest;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBuildContentId(String buildContentId) {
        this.buildContentId = buildContentId;
    }

    public void setBuildScript(String buildScript) {
        this.buildScript = buildScript;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScmRepoURL(String scmRepoURL) {
        this.scmRepoURL = scmRepoURL;
    }

    public void setScmRevision(String scmRevision) {
        this.scmRevision = scmRevision;
    }

    public void setOriginRepoURL(String originRepoURL) {
        this.originRepoURL = originRepoURL;
    }

    public void setPreBuildSyncEnabled(boolean preBuildSyncEnabled) {
        this.preBuildSyncEnabled = preBuildSyncEnabled;
    }

    @Override
    public int getId() {
        return id;
    }

    @JsonIgnore
    @Override
    public Integer getUserId() {
        return user.getId();
    }

    @Override
    public String getBuildContentId() {
        return buildContentId;
    }

    @Override
    public String getBuildScript() {
        return buildScript;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getScmRepoURL() {
        return scmRepoURL;
    }

    @Override
    public String getScmRevision() {
        return scmRevision;
    }

    @Override
    public String getScmTag() {
        return scmTag;
    }

    @Override
    public String getOriginRepoURL() {
        return originRepoURL;
    }

    @Override
    public boolean isPreBuildSyncEnabled() {
        return preBuildSyncEnabled;
    }

    public BuildType getBuildType() {
        return buildType;
    }

    public UserRest getUser() {
        return user;
    }

    public void setUser(UserRest user) {
        this.user = user;
    }

    public void setBuildType(BuildType buildType) {
        this.buildType = buildType;
    }

    @Override
    public String getSystemImageId() {
        return systemImageId;
    }

    public void setSystemImageId(String systemImageId) {
        this.systemImageId = systemImageId;
    }

    @Override
    public String getSystemImageRepositoryUrl() {
        return systemImageRepositoryUrl;
    }

    public void setSystemImageRepositoryUrl(String systemImageRepositoryUrl) {
        this.systemImageRepositoryUrl = systemImageRepositoryUrl;
    }

    @Override
    public SystemImageType getSystemImageType() {
        return systemImageType;
    }

    @Override
    public boolean isPodKeptOnFailure() {
        return podKeptOnFailure;
    }

    public void setPodKeptOnFailure(boolean podKeptOnFailure) {
        this.podKeptOnFailure = podKeptOnFailure;
    }

    public void setSystemImageType(SystemImageType systemImageType) {
        this.systemImageType = systemImageType;
    }

    @Override
    public List<ArtifactRepository> getArtifactRepositories() {
        return artifactRepositories;
    }

    public void setArtifactRepositories(List<ArtifactRepositoryRest> artifactRepositoriesRest) {
        this.artifactRepositories = new ArrayList<>(artifactRepositoriesRest);
    }

    public void setGenericParameters(Map<String, String> genericParameters) {
        this.genericParameters = genericParameters;
    }

    @Override
    public Map<String, String> getGenericParameters() {
        return genericParameters;
    }

    @Override
    public boolean isTempBuild() {
        return tempBuild;
    }

    @Override
    public String toString() {
        return JsonOutputConverterMapper.apply(this);
    }

}
