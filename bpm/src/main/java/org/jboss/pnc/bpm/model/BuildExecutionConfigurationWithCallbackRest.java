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

package org.jboss.pnc.bpm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jboss.pnc.dto.User;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;

import javax.xml.bind.annotation.XmlRootElement;
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
@XmlRootElement(name = "buildExecutionConfiguration")
public class BuildExecutionConfigurationWithCallbackRest extends BuildExecutionConfigurationRest {

    private String completionCallbackUrl;

    public BuildExecutionConfigurationWithCallbackRest(BuildExecutionConfiguration buildExecutionConfiguration) {
        super(buildExecutionConfiguration);
    }

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
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
            String defaultAlignmentParams) {
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
                defaultAlignmentParams);
        this.completionCallbackUrl = completionCallbackUrl;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }

}
