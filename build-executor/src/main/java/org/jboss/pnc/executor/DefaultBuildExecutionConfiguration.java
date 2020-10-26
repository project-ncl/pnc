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

package org.jboss.pnc.executor;

import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.repositorymanager.ArtifactRepository;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DefaultBuildExecutionConfiguration implements BuildExecutionConfiguration {

    private final int id;
    private final String buildContentId;
    private final String userId;
    private final String buildScript;
    private final String name;
    private final String scmRepoURL;
    private final String scmRevision;
    private final String scmTag;
    private final String originRepoURL;
    private final boolean preBuildSyncEnabled;
    private final BuildType buildType;
    private final String systemImageId;
    private final String systemImageRepositoryUrl;
    private final SystemImageType systemImageType;
    private final boolean podKeptAfterFailure;
    private final List<ArtifactRepository> artifactRepositories;
    private final Map<String, String> genericParameters;
    private final boolean tempBuild;
    private final String tempBuildTimestamp;
    private final boolean brewPullActive;
    private final String defaultAlignmentParams;

    public DefaultBuildExecutionConfiguration(
            int id,
            String buildContentId,
            String userId,
            String buildScript,
            String name,
            String scmRepoURL,
            String scmRevision,
            String scmTag,
            String originRepoURL,
            boolean preBuildSyncEnabled,
            BuildType buildType,
            String systemImageId,
            String systemImageRepositoryUrl,
            SystemImageType systemImageType,
            boolean podKeptAfterFailure,
            List<ArtifactRepository> artifactRepositories,
            Map<String, String> genericParameters,
            boolean tempBuild,
            String tempBuildTimestamp,
            boolean brewPullActive,
            String defaultAlignmentParams) {

        this.id = id;
        this.buildContentId = buildContentId;
        this.userId = userId;
        this.buildScript = buildScript;
        this.name = name;
        this.scmRepoURL = scmRepoURL;
        this.scmRevision = scmRevision;
        this.scmTag = scmTag;
        this.originRepoURL = originRepoURL;
        this.preBuildSyncEnabled = preBuildSyncEnabled;
        this.buildType = buildType;
        this.systemImageId = systemImageId;
        this.systemImageRepositoryUrl = systemImageRepositoryUrl;
        this.systemImageType = systemImageType;
        this.podKeptAfterFailure = podKeptAfterFailure;
        this.artifactRepositories = artifactRepositories;
        this.genericParameters = genericParameters;
        this.tempBuild = tempBuild;
        this.tempBuildTimestamp = tempBuildTimestamp;
        this.brewPullActive = brewPullActive;
        this.defaultAlignmentParams = defaultAlignmentParams;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getBuildContentId() {
        return buildContentId;
    }

    @Override
    public String getUserId() {
        return userId;
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

    @Override
    public BuildType getBuildType() {
        return buildType;
    }

    @Override
    public String getSystemImageId() {
        return systemImageId;
    }

    @Override
    public String getSystemImageRepositoryUrl() {
        return systemImageRepositoryUrl;
    }

    @Override
    public SystemImageType getSystemImageType() {
        return systemImageType;
    }

    @Override
    public boolean isPodKeptOnFailure() {
        return podKeptAfterFailure;
    }

    @Override
    public List<ArtifactRepository> getArtifactRepositories() {
        return artifactRepositories;
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
    public String getTempBuildTimestamp() {
        return tempBuildTimestamp;
    }

    @Override
    public boolean isBrewPullActive() {
        return brewPullActive;
    }

    @Override
    public String getDefaultAlignmentParams() {
        return defaultAlignmentParams;
    }
}
