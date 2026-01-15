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
package org.jboss.pnc.rest.endpoints;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.api.bifrost.enums.Format;
import org.jboss.pnc.api.constants.MDCKeys;
import org.jboss.pnc.api.dto.ExceptionResolution;
import org.jboss.pnc.api.dto.Result;
import org.jboss.pnc.api.enums.ResultStatus;
import org.jboss.pnc.auth.ServiceAccountClient;
import org.jboss.pnc.common.http.HttpUtils;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.constants.Attributes;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildPushOperation;
import org.jboss.pnc.dto.BuildPushReport;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.dto.insights.BuildRecordInsights;
import org.jboss.pnc.api.causeway.dto.push.BuildPushCompleted;
import org.jboss.pnc.dto.requests.BuildPushParameters;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.RunningBuildCount;
import org.jboss.pnc.dto.response.SSHCredentials;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildPushStatus;
import org.jboss.pnc.facade.BrewPusher;
import org.jboss.pnc.facade.BuildTriggerer;
import org.jboss.pnc.facade.providers.api.ArtifactProvider;
import org.jboss.pnc.facade.providers.api.BuildPageInfo;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.facade.providers.api.BuildPushOperationProvider;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.utils.ContentIdentityManager;
import org.jboss.pnc.rest.api.endpoints.BuildEndpoint;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.text.MessageFormat.format;
import static org.jboss.pnc.common.util.StringUtils.stripEndingSlash;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@ApplicationScoped
@Slf4j
public class BuildEndpointImpl implements BuildEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(BuildEndpointImpl.class);

    /**
     * Param 1: build-id
     * <p>
     * Param 2: build-log/alignment-log
     */
    private static final String BIFROST_LOGS_ENDPOINT = "/final-log/{0}/{1}";

    public static BuildPageInfo toBuildPageInfo(PageParameters page, BuildsFilterParameters builds) {
        return new BuildPageInfo(
                page.getPageIndex(),
                page.getPageSize(),
                page.getSort(),
                page.getQ(),
                builds.isLatest(),
                builds.isRunning(),
                builds.getBuildConfigName());
    }

    @Inject
    private BuildProvider provider;

    @Inject
    private ArtifactProvider artifactProvider;

    @Inject
    private BuildTriggerer buildTriggerer;

    @Inject
    private BrewPusher brewPusher;

    @Inject
    private GlobalModuleGroup globalConfig;

    @Inject
    private ServiceAccountClient serviceAccountClient;

    @Inject
    private ManagedExecutorService executorService;

    @Inject
    private BuildPushOperationProvider buildPushOperationProvider;

    private EndpointHelper<Base32LongID, Build, BuildRef> endpointHelper;

    @PostConstruct
    public void init() {
        endpointHelper = new EndpointHelper<>(Build.class, provider);
    }

    @Override
    public Build getSpecific(String id) {
        return endpointHelper.getSpecific(id);
    }

    @Override
    public void delete(String id, String callback) {
        if (!provider.delete(id, callback)) {
            throw new NotFoundException("Temporary build with id: " + id + " was not found.");
        }
    }

    @Override
    public void update(String id, Build build) {
        endpointHelper.update(id, build);
    }

    @Override
    public Page<Build> getAll(PageParameters pageParams, BuildsFilterParameters filterParams, List<String> attributes) {
        if (attributes != null && !attributes.isEmpty()) {
            Map<String, String> attributeConstraints = parseAttributes(attributes);
            return provider.getByAttribute(toBuildPageInfo(pageParams, filterParams), attributeConstraints);
        } else {
            return provider.getBuilds(toBuildPageInfo(pageParams, filterParams));
        }
    }

    private Map<String, String> parseAttributes(List<String> attributes) {
        Map<String, String> map = new HashMap<>();
        for (String attribute : attributes) {
            String[] kv = attribute.split(":");
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
            } else if (kv.length == 1) {
                map.put(kv[0], "");
            } else {
                throw new BadRequestException("Invalid 'attributes' query parameters.");
            }
        }
        return map;
    }

    @Override
    public Page<Artifact> getBuiltArtifacts(String id, PageParameters pageParameters) {
        return artifactProvider.getBuiltArtifactsForBuild(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ(),
                id);
    }

    @Override
    public void setBuiltArtifacts(String id, List<String> artifactIds) {
        provider.setBuiltArtifacts(id, artifactIds);
    }

    @Override
    public void createBuiltArtifactsQualityLevelRevisions(String id, String quality, String reason) {
        Set<String> builtArtifactIds = provider.getBuiltArtifactIds(id);
        for (String builtArtifactId : builtArtifactIds) {
            artifactProvider.createQualityLevelRevision(builtArtifactId, quality, reason);
        }
        ArtifactQuality newQuality = ArtifactQuality.valueOf(quality.toUpperCase());
        if (ArtifactQuality.DELETED.equals(newQuality)) {
            provider.addAttribute(id, Attributes.DELETE_REASON, reason);
        } else if (ArtifactQuality.BLACKLISTED.equals(newQuality)) {
            provider.addAttribute(id, Attributes.BLACKLIST_REASON, reason);
        }
    }

    @Override
    public Page<Artifact> getDependencyArtifacts(String id, PageParameters pageParameters) {
        return artifactProvider.getDependantArtifactsForBuild(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ(),
                id);
    }

    public Page<Artifact> getAttachedArtifacts(String id, PageParameters pageParameters) {
        return artifactProvider.getAttachedArtifactsForBuild(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ(),
                id);
    }

    @Override
    public void setDependentArtifacts(String id, List<String> artifactIds) {
        provider.setDependentArtifacts(id, artifactIds);
    }

    @Override
    public Response getInternalScmArchiveLink(String id) {
        URI toRedirect = provider.getInternalScmArchiveLink(id);

        if (toRedirect == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.temporaryRedirect(toRedirect).build();
        }
    }

    @Override
    public void addAttribute(String id, String key, String value) {
        provider.addAttribute(id, key, value);
    }

    @Override
    public void removeAttribute(String id, String key) {
        provider.removeAttribute(id, key);
    }

    @Override
    public BuildPushReport getPushResult(String buildId) {
        BuildPushReport brewPushResult = brewPusher.getBrewPushResult(buildId);
        if (brewPushResult == null) {
            throw new NotFoundException();
        }

        return new BuildPushReportCompatibility(brewPushResult);
    }

    @Override
    public Page<BuildPushOperation> getPushOperations(String buildId, PageParameters pageParameters) {
        return buildPushOperationProvider.getOperationsForBuild(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ(),
                buildId);
    }

    @Value
    @EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
    @Deprecated(forRemoval = true, since = "3.2")
    // TODO: when removing, remove @AllArgsConstructor(access = AccessLevel.PROTECTED) from BuildPushReport
    private static class BuildPushReportCompatibility extends BuildPushReport {
        Object productMilestoneCloseResult = null;
        String buildId;
        BuildPushStatus status;
        String logContext;
        String message = null;
        String userInitiator;

        public BuildPushReportCompatibility(BuildPushReport buildPushReport) {
            super(
                    buildPushReport.getId(),
                    buildPushReport.getSubmitTime(),
                    buildPushReport.getStartTime(),
                    buildPushReport.getEndTime(),
                    buildPushReport.getUser(),
                    buildPushReport.getProgressStatus(),
                    buildPushReport.getResult(),
                    buildPushReport.getBuild(),
                    buildPushReport.getTagPrefix(),
                    buildPushReport.getBrewBuildId(),
                    buildPushReport.getBrewBuildUrl());

            buildId = buildPushReport.getBuild().getId();
            logContext = buildPushReport.getBuild().getId();
            userInitiator = buildPushReport.getUser().getUsername();
            if (buildPushReport.getResult() == null) {
                status = BuildPushStatus.ACCEPTED;
            } else {
                switch (buildPushReport.getResult()) {
                    case SUCCESSFUL:
                        status = BuildPushStatus.SUCCESS;
                        break;
                    case FAILED:
                        status = BuildPushStatus.FAILED;
                        break;
                    case REJECTED:
                        status = BuildPushStatus.REJECTED;
                        break;
                    case CANCELLED:
                        status = BuildPushStatus.CANCELED;
                        break;
                    case TIMEOUT:
                    case SYSTEM_ERROR:
                    default:
                        status = BuildPushStatus.SYSTEM_ERROR;
                }
            }
        }

    }

    @Override
    public BuildPushOperation push(String id, BuildPushParameters buildPushParameters) {
        BuildPushOperation buildPushOperation = brewPusher.pushBuild(id, buildPushParameters);
        BuildPushOperationCompatibility compat = new BuildPushOperationCompatibility(buildPushOperation.toBuilder());
        return compat;
    }

    @Value
    @EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
    @Deprecated(forRemoval = true, since = "3.2")
    private static class BuildPushOperationCompatibility extends BuildPushOperation {
        Object productMilestoneCloseResult = null;
        String buildId;
        BuildPushStatus status = BuildPushStatus.ACCEPTED;
        Integer brewBuildId = null;
        String brewBuildUrl = null;
        String logContext;
        String message = null;
        String userInitiator;

        public BuildPushOperationCompatibility(BuildPushOperationBuilder<?, ?> b) {
            super(b);
            buildId = getBuild().getId();
            logContext = getBuild().getId();
            userInitiator = getUser().getUsername();
        }
    }

    @Override
    public void cancelPush(String id) {
        brewPusher.cancelPushOfBuild(id);
    }

    @Override
    public void completePush(String id, BuildPushCompleted buildPushCompleted) {
        executorService.execute(() -> {
            ResultStatus result;
            ExceptionResolution exceptionResolution = null;
            try {
                brewPusher.brewPushComplete(id, buildPushCompleted);
                result = ResultStatus.SUCCESS;
            } catch (RuntimeException e) {
                log.error("Storing results of build push with id={} failed: ", buildPushCompleted.getOperationId(), e);
                result = ResultStatus.SYSTEM_ERROR;
                exceptionResolution = ExceptionResolution.builder()
                        .reason(
                                String.format(
                                        "Storing results of deliverable operation with id=%s failed",
                                        buildPushCompleted.getOperationId()))
                        .proposal("Contact PNC IT Support")
                        .build();
            }

            HttpUtils.performHttpRequest(
                    buildPushCompleted.getCallback(),
                    new Result(result, exceptionResolution),
                    Optional.of(serviceAccountClient.getAuthHeaderValue()));
        });
    }

    @Override
    public BuildConfigurationRevision getBuildConfigRevision(String id) {
        return provider.getBuildConfigurationRevision(id);
    }

    @Override
    public void cancel(String buildId) {
        try {
            MDC.put(MDCKeys.PROCESS_CONTEXT_KEY, ContentIdentityManager.getBuildContentId(buildId));
            MDC.put(MDCKeys.BUILD_ID_KEY, buildId);
            logger.debug("Received cancel request for buildTaskId: {}.", buildId);

            if (!buildTriggerer.cancelBuild(buildId)) {
                throw new NotFoundException();
            }
            logger.debug("Cancel request for buildTaskId {} successfully processed.", buildId);
        } catch (CoreException e) {
            logger.error("Unable to cancel the build [" + buildId + "].", e);
            throw new RuntimeException("Unable to cancel the build [" + buildId + "].", e);
        } finally {
            MDC.remove(MDCKeys.PROCESS_CONTEXT_KEY);
            MDC.remove(MDCKeys.BUILD_ID_KEY);
        }
    }

    @Override
    public Graph<Build> getDependencyGraph(String id) {
        return provider.getDependencyGraph(id);
    }

    @Override
    public StreamingOutput getAlignLogs(String id) {
        throw new RedirectionException(
                Response.Status.TEMPORARY_REDIRECT,
                createBifrostRedirectURL(id, "alignment-log", Format.LEVEL));
    }

    @Override
    public StreamingOutput getBuildLogs(String id) {
        throw new RedirectionException(
                Response.Status.TEMPORARY_REDIRECT,
                createBifrostRedirectURL(id, "build-log", Format.PLAIN));
    }

    private URI createBifrostRedirectURL(String buildID, String logType, Format format) {
        String bifrostURL = stripEndingSlash(globalConfig.getExternalBifrostUrl());
        return URI.create(bifrostURL.concat(format(BIFROST_LOGS_ENDPOINT, buildID, logType)));
    }

    @Override
    public SSHCredentials getSshCredentials(String id) {
        return provider.getSshCredentials(id);
    }

    @Override
    public RunningBuildCount getCount() {
        return provider.getRunningCount();
    }

    @Override
    public Page<Build> getAllIndependentTempBuildsOlderThanTimestamp(PageParameters pageParams, long timestamp) {
        return provider.getAllIndependentTemporaryOlderThanTimestamp(
                pageParams.getPageIndex(),
                pageParams.getPageSize(),
                pageParams.getSort(),
                pageParams.getQ(),
                timestamp);
    }

    @Override
    public Page<BuildRecordInsights> getAllBuildRecordInsightsNewerThanTimestamp(
            int pageSize,
            int pageIndex,
            long timestamp) {

        return provider.getAllBuildRecordInsightsNewerThanTimestamp(pageIndex, pageSize, new Date(timestamp));
    }

    @Override
    public Graph<Build> getImplicitDependencyGraph(String buildId, Integer depthLimit) {
        return provider.getImplicitDependencyGraph(buildId, depthLimit);
    }
}
