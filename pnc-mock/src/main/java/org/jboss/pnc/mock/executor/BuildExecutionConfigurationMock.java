/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

package org.jboss.pnc.mock.executor;

import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.repositorymanager.ArtifactRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildExecutionConfigurationMock implements BuildExecutionConfiguration {

    private int id;
    private String buildContentId;
    private String userId;
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
    private List<ArtifactRepository> artifactRepositories;
    private Map<String, String> genericParameters;
    private boolean tempBuild;
    private String tempBuildTimestamp;
    private String defaultAlignmentParams;

    public static BuildExecutionConfiguration mockConfig() {
        BuildExecutionConfigurationMock mock = new BuildExecutionConfigurationMock();
        mock.setId(1);
        mock.setBuildScript("mvn install");
        mock.setScmRepoURL("http://www.github.com");
        mock.setScmRevision("f18de64523d5054395d82e24d4e28473a05a3880");
        mock.setScmTag("1.0.0.redhat-1");
        mock.setPreBuildSyncEnabled(false);
        mock.setBuildType(BuildType.MVN);
        mock.setSystemImageId("abcd1234");
        mock.setSystemImageRepositoryUrl("image.repo.url/repo");
        mock.setSystemImageType(SystemImageType.DOCKER_IMAGE);
        mock.setArtifactRepositories(null);
        mock.setGenericParameters(new HashMap<>());
        mock.setTempBuild(false);
        mock.setTempBuildTimestamp(null);
        mock.setDefaultAlignmentParams(
                "-DdependencySource=REST -DrepoRemovalBackup=repositories-backup.xml -DversionSuffixStrip= -DreportNonAligned=true");

        return mock;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getBuildContentId() {
        return buildContentId;
    }

    public void setBuildContentId(String buildContentId) {
        this.buildContentId = buildContentId;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getBuildScript() {
        return buildScript;
    }

    public void setBuildScript(String buildScript) {
        this.buildScript = buildScript;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getScmRepoURL() {
        return scmRepoURL;
    }

    public void setScmRepoURL(String scmRepoURL) {
        this.scmRepoURL = scmRepoURL;
    }

    @Override
    public String getScmRevision() {
        return scmRevision;
    }

    @Override
    public String getScmTag() {
        return scmTag;
    }

    public void setScmTag(String scmTag) {
        this.scmTag = scmTag;
    }

    @Override
    public String getOriginRepoURL() {
        return originRepoURL;
    }

    public void setOriginRepoURL(String originRepoURL) {
        this.originRepoURL = originRepoURL;
    }

    public void setScmRevision(String scmRevision) {
        this.scmRevision = scmRevision;
    }

    @Override
    public boolean isPreBuildSyncEnabled() {
        return preBuildSyncEnabled;
    }

    public void setPreBuildSyncEnabled(boolean preBuildSyncEnabled) {
        this.preBuildSyncEnabled = preBuildSyncEnabled;
    }

    public void setBuildType(BuildType buildType) {
        this.buildType = buildType;
    }

    @Override
    public BuildType getBuildType() {
        return buildType;
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
        return false;
    }

    public void setSystemImageType(SystemImageType systemImageType) {
        this.systemImageType = systemImageType;
    }

    @Override
    public List<ArtifactRepository> getArtifactRepositories() {
        return artifactRepositories;
    }

    public void setArtifactRepositories(List<ArtifactRepository> artifactRepositories) {
        this.artifactRepositories = artifactRepositories;
    }

    @Override
    public Map<String, String> getGenericParameters() {
        return genericParameters;
    }

    public void setGenericParameters(Map<String, String> genericParameters) {
        this.genericParameters = genericParameters;
    }

    @Override
    public boolean isTempBuild() {
        return tempBuild;
    }

    public void setTempBuild(boolean tempBuild) {
        this.tempBuild = tempBuild;
    }

    @Override
    public String getTempBuildTimestamp() {
        return tempBuildTimestamp;
    }

    public void setTempBuildTimestamp(String tempBuildTimestamp) {
        this.tempBuildTimestamp = tempBuildTimestamp;
    }

    public String getDefaultAlignmentParams() {
        return defaultAlignmentParams;
    }

    public void setDefaultAlignmentParams(String defaultAlignmentParams) {
        this.defaultAlignmentParams = defaultAlignmentParams;
    }

}
