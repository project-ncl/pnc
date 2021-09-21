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

package org.jboss.pnc.spi.executor;

import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.spi.repositorymanager.ArtifactRepository;
import org.jboss.pnc.spi.repositorymanager.BuildExecution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    String getOriginRepoURL();

    boolean isPreBuildSyncEnabled();

    String getSystemImageId();

    String getSystemImageRepositoryUrl();

    SystemImageType getSystemImageType();

    boolean isPodKeptOnFailure();

    Map<String, String> getGenericParameters();

    String getDefaultAlignmentParams();

    static BuildExecutionConfiguration build(
            String id,
            String buildContentId,
            String userId,
            String buildScript,
            String buildConfigurationId,
            String name,
            String scmRepoURL,
            String scmRevision,
            String scmTag,
            String originRepoURL,
            boolean preBuildSyncEnabled,
            String systemImageId,
            String systemImageRepositoryUrl,
            SystemImageType systemImageType,
            BuildType buildType,
            boolean podKeptAfterFailure,
            Map<String, String> genericParameters,
            boolean tempBuild,
            String tempBuildTimestamp,
            boolean brewPullActive,
            String defaultAlignmentParams) {
        return build(
                id,
                buildContentId,
                userId,
                buildScript,
                buildConfigurationId,
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
                podKeptAfterFailure,
                null,
                genericParameters,
                tempBuild,
                tempBuildTimestamp,
                brewPullActive,
                defaultAlignmentParams);
    }

    static BuildExecutionConfiguration build(
            String id,
            String buildContentId,
            String userId,
            String buildScript,
            String buildConfigurationId,
            String name,
            String scmRepoURL,
            String scmRevision,
            String scmTag,
            String originRepoURL,
            boolean preBuildSyncEnabled,
            String systemImageId,
            String systemImageRepositoryUrl,
            SystemImageType systemImageType,
            BuildType buildType,
            boolean podKeptAfterFailure,
            List<ArtifactRepository> artifactRepositories,
            Map<String, String> genericParameters,
            boolean tempBuild,
            String tempBuildTimestamp,
            boolean brewPullActive,
            String defaultAlignmentParams) {

        List<ArtifactRepository> builtRepositories;
        if (artifactRepositories == null) {
            builtRepositories = null;
        } else {
            builtRepositories = new ArrayList<>(artifactRepositories.size());
            for (ArtifactRepository artifactRepository : artifactRepositories) {
                builtRepositories.add(
                        ArtifactRepository.build(
                                artifactRepository.getId(),
                                artifactRepository.getName(),
                                artifactRepository.getUrl(),
                                artifactRepository.getReleases(),
                                artifactRepository.getSnapshots()));
            }
        }

        return new BuildExecutionConfiguration() {

            @Override
            public String getId() {
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
            public String getBuildConfigurationId() {
                return buildConfigurationId;
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
            public String getSystemImageId() {
                return systemImageId;
            }

            @Override
            public BuildType getBuildType() {
                return buildType;
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
                return builtRepositories;
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
            public boolean isBrewPullActive() {
                return brewPullActive;
            }

            @Override
            public String getTempBuildTimestamp() {
                return tempBuildTimestamp;
            }

            @Override
            public String getDefaultAlignmentParams() {
                return defaultAlignmentParams;
            }
        };
    }

}
