/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.dto.User;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.enums.SystemImageType;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder")
public class BuildExecutionConfiguration {

    private final Integer id;
    private final String buildContentId;
    private final User user;
    private final String buildScript;
    private final String name;

    private final String scmRepoURL;
    private final String scmRevision;
    private final String originRepoURL;
    private final Boolean preBuildSyncEnabled;

    private final BuildType buildType;
    private final String systemImageId;
    private final String systemImageRepositoryUrl;
    private final SystemImageType systemImageType;
    private final Boolean podKeptOnFailure = false;
    private final List<ArtifactRepository> artifactRepositories;
    private final Map<String, String> genericParameters;

    private final Boolean tempBuild;

    private final String tempBuildTimestamp;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
