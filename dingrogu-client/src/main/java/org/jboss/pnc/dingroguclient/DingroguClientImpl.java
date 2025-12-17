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

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.pnc.api.constants.BuildConfigurationParameterKeys;
import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.api.enums.BuildCategory;
import org.jboss.pnc.api.enums.BuildType;
import org.jboss.pnc.auth.ServiceAccountClient;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.log.MDCUtils;
import org.jboss.pnc.common.util.HttpUtils;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.utils.ContentIdentityManager;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DingroguClientImpl implements DingroguClient {
    private static final int MAX_RETRIES = 5;

    @Inject
    private GlobalModuleGroup global;

    @Inject
    private ServiceAccountClient serviceAccountClient;

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
                DingroguClient.addMdcValues(headers),
                dto);
    }

    @Override
    public void submitDeliverablesAnalysis(DingroguDeliverablesAnalysisDTO dto) {
        String url = global.getExternalDingroguUrl() + "/workflow/deliverables-analysis/start";
        submitRequestWithRetries(
                Request.builder().method(Request.Method.POST).uri(URI.create(url)).build(),
                dto,
                Optional.of(serviceAccountClient.getAuthHeaderValue()));
    }

    @Override
    public void submitBuildPush(DingroguBuildPushDTO dto) {
        String url = global.getExternalDingroguUrl() + "/workflow/brew-push/start";
        submitRequestWithRetries(
                Request.builder().method(Request.Method.POST).uri(URI.create(url)).build(),
                dto,
                Optional.of(serviceAccountClient.getAuthHeaderValue()));
    }

    @Override
    public void submitRepositoryCreation(DingroguRepositoryCreationDTO dto) {
        String url = global.getExternalDingroguUrl() + "/workflow/repository-creation/start";
        submitRequestWithRetries(
                Request.builder().method(Request.Method.POST).uri(URI.create(url)).build(),
                dto,
                Optional.of(serviceAccountClient.getAuthHeaderValue()));
    }

    @Override
    public Request cancelProcessInstance(List<Request.Header> headers, String correlationId) {

        return new Request(
                Request.Method.POST,
                URI.create(global.getExternalDingroguUrl() + "/workflow/id/" + correlationId + "/cancel"),
                DingroguClient.addMdcValues(headers),
                null);
    }

    @Override
    public void submitCancelProcessInstance(String correlationId) {
        String url = global.getExternalDingroguUrl() + "/workflow/id/" + correlationId + "/cancel";
        submitRequestWithRetries(
                Request.builder().method(Request.Method.POST).uri(URI.create(url)).build(),
                null,
                Optional.of(serviceAccountClient.getAuthHeaderValue()));
    }

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
                .buildDriverUrl(global.getExternalBuildDriverUrl())
                .environmentDriverUrl(global.getExternalEnvironmentDriverUrl())
                .scmRepoURL(buildTask.getBuildConfigurationAudited().getRepositoryConfiguration().getInternalUrl())
                .scmRevision(buildTask.getBuildConfigurationAudited().getScmRevision())
                .preBuildSyncEnabled(
                        buildTask.getBuildConfigurationAudited().getRepositoryConfiguration().isPreBuildSyncEnabled())
                .originRepoURL(buildTask.getBuildConfigurationAudited().getRepositoryConfiguration().getExternalUrl())
                .tempBuild(buildTask.getBuildOptions().isTemporaryBuild())
                .alignmentPreference(buildTask.getBuildOptions().getAlignmentPreference())
                .buildContentId(contentId)
                .buildConfigName(buildTask.getBuildConfigurationAudited().getBuildConfiguration().getName())
                .buildType(
                        BuildType.valueOf(
                                buildTask.getBuildConfigurationAudited().getBuildConfiguration().getBuildType().name()))
                .buildCategory(getBuildCategory(buildTask.getBuildConfigurationAudited().getGenericParameters()))
                .defaultAlignmentParams(buildTask.getBuildConfigurationAudited().getDefaultAlignmentParams())
                .brewPullActive(buildTask.getBuildConfigurationAudited().isBrewPullActive())
                .genericParameters(buildTask.getBuildConfigurationAudited().getGenericParameters())
                .buildConfigurationId(buildTask.getBuildConfigurationAudited().getId().toString())
                .buildScript(buildTask.getBuildConfigurationAudited().getBuildConfiguration().getBuildScript())
                .correlationId(correlationId)
                .podMemoryOverride(podMemoryOverride)
                .environmentImage(getEnvironmentImage(buildTask.getBuildConfigurationAudited().getBuildEnvironment()))
                .environmentLabel(buildTask.getId())
                .debugEnabled(buildTask.getBuildOptions().isKeepPodOnFailure())
                .build();
    }

    /**
     * return the full docker image url to use
     * 
     * @param buildEnvironment
     * @return
     */
    private static String getEnvironmentImage(BuildEnvironment buildEnvironment) {
        String repositoryUrl = buildEnvironment.getSystemImageRepositoryUrl();
        String imageId = buildEnvironment.getSystemImageId();

        if (repositoryUrl.endsWith("/")) {
            return repositoryUrl + imageId;
        } else {
            return repositoryUrl + "/" + imageId;
        }
    }

    private static BuildCategory getBuildCategory(Map<String, String> genericParameters) {

        if (genericParameters == null) {
            return BuildCategory.STANDARD;
        }

        String buildCategoryKey = BuildConfigurationParameterKeys.BUILD_CATEGORY.name();
        return BuildCategory.SERVICE.name().equals(genericParameters.get(buildCategoryKey)) ? BuildCategory.SERVICE
                : BuildCategory.STANDARD;
    }

    public static void submitRequestWithRetries(Request request, Object payload, Optional<String> authHeaderValue) {
        submitRequestWithRetriesAttempt(request, payload, authHeaderValue, MAX_RETRIES);
    }

    public static void submitRequestWithRetriesAttempt(
            Request request,
            Object payload,
            Optional<String> authHeaderValue,
            int retries) {

        if (retries < 0) {
            throw new RuntimeException("Maximum number of retries attempted!");
        }

        try {

            // Add MDC values, always
            List<Request.Header> headers = MDCUtils.getHeadersFromMDC()
                    .entrySet()
                    .stream()
                    .map(entry -> new Request.Header(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());

            request.getHeaders().addAll(headers);

            HttpUtils.performHttpRequest(request, payload, authHeaderValue);
        } catch (Exception e) {
            log.error("Exception for request: {}, attempts left: {}", request.getUri(), retries, e);
            try {
                Thread.sleep((long) (MAX_RETRIES + 1 - retries) * 1000L);
            } catch (InterruptedException er) {
                log.error("Exception thrown during sleeping", er);
            }

            // retry again
            submitRequestWithRetriesAttempt(request, payload, authHeaderValue, retries - 1);
        }
    }
}
