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
import org.jboss.pnc.auth.KeycloakServiceClient;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.util.HttpUtils;
import org.jboss.pnc.model.utils.ContentIdentityManager;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class DingroguClientImpl implements DingroguClient {
    private static final int MAX_RETRIES = 5;

    @Inject
    private GlobalModuleGroup global;

    @Inject
    private KeycloakServiceClient keycloakServiceClient;

    public static final Logger log = LoggerFactory.getLogger(DingroguClientImpl.class);

    @Override
    public Request startBuildProcessInstance(
            RemoteBuildTask buildTask,
            List<Request.Header> headers,
            String correlationId) {
        DingroguBuildWorkDTO dto = createDTO(buildTask, correlationId);
        return new Request(
                Request.Method.POST,
                URI.create(global.getExternalDingroguUrl() + "/workflow/build/rex-start"),
                headers,
                dto);
    }

    @Override
    public void submitDeliverablesAnalysis(DingroguDeliverablesAnalysisDTO dto) {
        String url = global.getExternalDingroguUrl() + "/workflow/deliverables-analysis/start";
        submitRequestWithRetries(
                Request.builder().method(Request.Method.POST).uri(URI.create(url)).build(),
                dto,
                Optional.of(keycloakServiceClient.getAuthToken()));
    }

    @Override
    public void submitBuildPush(DingroguBuildPushDTO dto) {
        String url = global.getExternalDingroguUrl() + "/workflow/brew-push/start";
        submitRequestWithRetries(
                Request.builder().method(Request.Method.POST).uri(URI.create(url)).build(),
                dto,
                Optional.of(keycloakServiceClient.getAuthToken()));
    }

    @Override
    public void submitRepositoryCreation(DingroguRepositoryCreationDTO dto) {
        String url = global.getExternalDingroguUrl() + "/workflow/repository-creation/start";
        submitRequestWithRetries(
                Request.builder().method(Request.Method.POST).uri(URI.create(url)).build(),
                dto,
                Optional.of(keycloakServiceClient.getAuthToken()));
    }

    @Override
    public Request cancelProcessInstance(List<Request.Header> headers, String correlationId) {

        return new Request(
                Request.Method.POST,
                URI.create(global.getExternalDingroguUrl() + "/workflow/id/" + correlationId + "/cancel"),
                headers,
                null);
    }

    /**
     * TODO: hardcoded values: buildToolVersion, javaVersion, buildTool
     * 
     * @param buildTask
     * @param correlationId
     * @return
     */
    @Override
    public DingroguBuildWorkDTO createDTO(RemoteBuildTask buildTask, String correlationId) {

        String podMemoryOverride = "";
        Map<String, String> genericParameters = buildTask.getBuildConfigurationAudited().getGenericParameters();
        if (genericParameters.containsKey("BUILDER_POD_MEMORY")) {
            podMemoryOverride = genericParameters.get("BUILDER_POD_MEMORY");
        }
        if (podMemoryOverride == null || podMemoryOverride.isBlank()) {
            // default value
            podMemoryOverride = "4";
        }
        String contentId = ContentIdentityManager.getBuildContentId(buildTask.getId());
        return DingroguBuildWorkDTO.builder()
                .reqourUrl(global.getExternalReqourUrl())
                .repositoryDriverUrl(global.getExternalRepositoryDriverUrl())
                .konfluxBuildDriverUrl(global.getExternalKonfluxBuildDriverUrl())
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
                .buildScript(buildTask.getBuildConfigurationAudited().getBuildConfiguration().getBuildScript())
                // TODO: temporary
                .javaVersion("17")
                // TODO: temporary
                .buildToolVersion("3.9.5")
                // TODO: temporary
                .buildTool("maven")
                .podMemoryOverride(podMemoryOverride)
                // TODO: temporary
                .recipeImage(global.getTempKonfluxRecipeImage())
                // TODO: temporary
                .namespace(global.getTempKonfluxNamespace())
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

    public static void submitRequestWithRetries(Request request, Object payload, Optional<String> authToken) {
        submitRequestWithRetriesAttempt(request, payload, authToken, MAX_RETRIES);
    }

    public static void submitRequestWithRetriesAttempt(
            Request request,
            Object payload,
            Optional<String> authToken,
            int retries) {

        if (retries < 0) {
            throw new RuntimeException("Maximum number of retries attempted!");
        }

        try {
            HttpUtils.performHttpRequest(request, payload, authToken);
        } catch (Exception e) {
            log.error("Exception for request: {}, attempts left: {}", request.getUri(), retries, e);
            try {
                Thread.sleep((long) (MAX_RETRIES + 1 - retries) * 1000L);
            } catch (InterruptedException er) {
                log.error("Exception thrown during sleeping", er);
            }

            // retry again
            submitRequestWithRetriesAttempt(request, payload, authToken, retries - 1);
        }
    }
}
