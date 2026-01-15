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
package org.jboss.pnc.spi.executor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.spi.repositorymanager.ArtifactRepository;

import java.util.List;
import java.util.Map;

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

    @Override
    public Boolean isScmBuildConfigRevisionInternal() {
        return getScmBuildConfigRevisionInternal();
    }
}
