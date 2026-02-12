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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
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
@ToString
@AllArgsConstructor
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder", toBuilder = true)
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildExecutionConfigurationRest implements Serializable {

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
    protected Boolean scmBuildConfigRevisionInternal;
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

}
