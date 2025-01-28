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

import org.jboss.pnc.api.constants.BuildConfigurationParameterKeys;
import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.api.enums.BuildCategory;
import org.jboss.pnc.api.enums.BuildType;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.model.utils.ContentIdentityManager;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URI;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DingroguClient {

    @Inject
    private GlobalModuleGroup global;

    public Request startProcessInstance(RemoteBuildTask buildTask, List<Request.Header> headers, String correlationId) {
        DingroguBuildWorkDTO dto = createDTO(buildTask, correlationId);
        return new Request(
                Request.Method.POST,
                URI.create(global.getExternalDingroguUrl() + "/workflow/build/rex-start"),
                headers,
                dto);
    }

    public Request cancelProcessInstance(List<Request.Header> headers, String correlationId) {

        return new Request(
                Request.Method.POST,
                URI.create(global.getExternalDingroguUrl() + "/workflow/id/" + correlationId + "/cancel"),
                headers,
                null);
    }

    public DingroguBuildWorkDTO createDTO(RemoteBuildTask buildTask, String correlationId) {

        String contentId = ContentIdentityManager.getBuildContentId(buildTask.getId());
        return DingroguBuildWorkDTO.builder()
                .reqourUrl(global.getExternalReqourUrl())
                .repositoryDriverUrl(global.getExternalRepositoryDriverUrl())
                .scmRepoURL(buildTask.getBuildConfigurationAudited().getRepositoryConfiguration().getInternalUrl())
                .scmRevision(buildTask.getBuildConfigurationAudited().getScmRevision())
                .preBuildSyncEnabled(
                        buildTask.getBuildConfigurationAudited().getRepositoryConfiguration().isPreBuildSyncEnabled())
                .originRepoURL(buildTask.getBuildConfigurationAudited().getRepositoryConfiguration().getExternalUrl())
                .tempBuild(buildTask.getBuildOptions().isTemporaryBuild())
                .alignmentPreference(buildTask.getBuildOptions().getAlignmentPreference())
                .buildContentId(contentId)
                .buildType(
                        BuildType.valueOf(
                                buildTask.getBuildConfigurationAudited().getBuildConfiguration().getBuildType().name()))
                .buildCategory(getBuildCategory(buildTask.getBuildConfigurationAudited().getGenericParameters()))
                .defaultAlignmentParams(buildTask.getBuildConfigurationAudited().getDefaultAlignmentParams())
                .brewPullActive(buildTask.getBuildConfigurationAudited().isBrewPullActive())
                .genericParameters(buildTask.getBuildConfigurationAudited().getGenericParameters())
                .buildConfigurationId(buildTask.getBuildConfigurationAudited().getId().toString())
                .correlationId(correlationId)
                .build();
    }

    private static BuildCategory getBuildCategory(Map<String, String> genericParameters) {

        if (genericParameters == null) {
            return BuildCategory.STANDARD;
        }

        String buildCategoryKey = BuildConfigurationParameterKeys.BUILD_CATEGORY.name();
        return BuildCategory.SERVICE.name().equals(genericParameters.get(buildCategoryKey)) ? BuildCategory.SERVICE
                : BuildCategory.STANDARD;
    }
}
