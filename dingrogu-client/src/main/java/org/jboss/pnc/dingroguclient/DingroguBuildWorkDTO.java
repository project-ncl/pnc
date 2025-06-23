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
package org.jboss.pnc.dingroguclient;

import java.util.Map;

import javax.validation.constraints.NotNull;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.api.enums.BuildCategory;
import org.jboss.pnc.api.enums.BuildType;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

// TODO: at some point sync this DTO with the one on the dingrogu url
@Jacksonized
@Data
@Builder
public class DingroguBuildWorkDTO {
    String reqourUrl;
    String repositoryDriverUrl;
    String buildDriverUrl;
    String environmentDriverUrl;

    String scmRepoURL;
    String scmRevision;
    boolean preBuildSyncEnabled;
    String originRepoURL;
    boolean tempBuild;
    AlignmentPreference alignmentPreference;
    @NotNull
    String buildContentId;
    String buildConfigName;
    BuildType buildType;
    BuildCategory buildCategory;
    String defaultAlignmentParams;
    boolean brewPullActive;
    Map<String, String> genericParameters;
    String buildConfigurationId;
    String correlationId;
    boolean debugEnabled;
    String environmentLabel;
    String environmentImage;

    String buildScript;
    String podMemoryOverride;
}