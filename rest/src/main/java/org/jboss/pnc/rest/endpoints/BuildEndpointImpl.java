/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.endpoints;

import static java.text.MessageFormat.format;
import static org.jboss.pnc.common.util.StringUtils.stripEndingSlash;
import static org.jboss.pnc.facade.providers.api.UserRoles.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.jboss.pnc.api.causeway.dto.push.BuildPushCompleted;
import org.jboss.pnc.api.constants.MDCKeys;
import org.jboss.pnc.api.reqour.rest.ScmContentEndpoint;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.constants.Attributes;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.Attachment;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildPushOperation;
import org.jboss.pnc.dto.BuildPushReport;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.dto.insights.BuildRecordInsights;
import org.jboss.pnc.dto.requests.BuildPushParameters;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.RunningBuildCount;
import org.jboss.pnc.dto.response.SSHCredentials;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.facade.BuildTriggerer;
import org.jboss.pnc.facade.providers.api.ArtifactProvider;
import org.jboss.pnc.facade.providers.api.AttachmentProvider;
import org.jboss.pnc.facade.providers.api.BuildPageInfo;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.facade.providers.api.ScmContentSource;
import org.jboss.pnc.facade.util.ReqourClient;
import org.jboss.pnc.facade.validation.BrewNotSupportedException;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.utils.ContentIdentityManager;
import org.jboss.pnc.rest.api.endpoints.BuildEndpoint;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@ApplicationScoped
@Slf4j
@RolesAllowed({ USERS_BUILD_READ, USERS, USERS_ADMIN })
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
    private AttachmentProvider attachmentProvider;

    @Inject
    private BuildTriggerer buildTriggerer;

    @Inject
    private GlobalModuleGroup globalConfig;

    @Inject
    private ReqourClient reqourClient;

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
    @RolesAllowed({ USERS_BUILD_DELETE, USERS_BUILD_ADMIN, USERS_ADMIN })
    public void delete(String id, String callback) {
        if (!provider.delete(id, callback)) {
            throw new NotFoundException("Temporary build with id: " + id + " was not found.");
        }
    }

    @Override
    @RolesAllowed({ USERS_BUILD_ADMIN, USERS_ADMIN })
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
    @RolesAllowed({ USERS_BUILD_ADMIN, USERS_ADMIN })
    public void setBuiltArtifacts(String id, List<String> artifactIds) {
        provider.setBuiltArtifacts(id, artifactIds);
    }

    @Override
    public Response getInternalScmArchive(String id) {
        ScmContentSource source = provider.getScmContentSource(id);
        if (source.getRevisions().isEmpty()) {
            throw new NotFoundException("Build " + id + " has no SCM revision.");
        }
        return fromReqour(
                id,
                source,
                url -> reqourClient.scmArchive(url, source.getRevisions()),
                "build-" + id + "-sources.tar.gz",
                ScmContentEndpoint.REVISION_HEADER);
    }

    @Override
    public Response getAlignmentDiff(String id) {
        ScmContentSource source = provider.getScmContentSource(id);
        if (source.getBases().isEmpty() || source.getHeads().isEmpty()) {
            throw new NotFoundException("Build " + id + " has no pre- or post-alignment SCM revision.");
        }
        return fromReqour(
                id,
                source,
                url -> reqourClient.scmDiff(url, source.getBases(), source.getHeads()),
                "build-" + id + "-alignment.diff",
                ScmContentEndpoint.DIFF_BASE_HEADER,
                ScmContentEndpoint.DIFF_HEAD_HEADER,
                ScmContentEndpoint.DIFF_MODE_HEADER);
    }

    /** Concurrent source and diff requests: each holds a worker thread and a reqour connection while streaming. */
    static final int MAX_CONCURRENT_SCM_CONTENT = 8;
    /**
     * Frees the permit and closes reqour's answer when the container never starts writing the response (it does so
     * right after the method returns).
     */
    private static final long PERMIT_SAFETY_RELEASE_MINUTES = 5;
    static final ScheduledThreadPoolExecutor PERMIT_RELEASER = permitReleaser();

    private final Semaphore scmContentPermits = new Semaphore(MAX_CONCURRENT_SCM_CONTENT);

    private static ScheduledThreadPoolExecutor permitReleaser() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = new Thread(r, "scm-content-permit-releaser");
            thread.setDaemon(true);
            return thread;
        });
        // a request leaves no task behind once its permit is released
        executor.setRemoveOnCancelPolicy(true);
        return executor;
    }

    /**
     * Streams reqour's answer for the first internal URL it can serve.
     * <ul>
     * <li>The next URL is tried when reqour answers 400 (URL not served) or 404 (revisions not found there). Its 502
     * and 504 (git provider error or timeout) are not retried: they do not depend on the URL.</li>
     * <li>Reqour's 400, 404, 502 and 504 are passed on, and so is its 503 (too many diffs at once) with its
     * {@code Retry-After}; any other status (its authentication errors with orch's service account, other 5xx) is a 502
     * here. Non-JSON error bodies (e.g. from a gateway) are replaced by an {@link ErrorResponse}, with a 502.</li>
     * <li>At most {@value #MAX_CONCURRENT_SCM_CONTENT} requests run at once; others get 503. The permit is held until
     * the transfer ends, however long it takes.</li>
     * </ul>
     */
    private Response fromReqour(
            String id,
            ScmContentSource source,
            Function<String, Response> call,
            String fileName,
            String... forwardedHeaders) {
        List<String> urls = source.getUrls();
        if (urls.isEmpty()) {
            throw new NotFoundException("Build " + id + " has no internal SCM repository.");
        }
        if (!scmContentPermits.tryAcquire()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .header(HttpHeaders.RETRY_AFTER, "30")
                    .entity(new ErrorResponse("TOO_MANY_REQUESTS", "Too many concurrent source and diff downloads."))
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .build();
        }
        Permit permit = new Permit(scmContentPermits);
        boolean streaming = false;
        try {
            for (Iterator<String> it = urls.iterator();;) {
                String url = it.next();
                Response response = call.apply(url);
                int status = response.getStatus();
                if (status == Response.Status.OK.getStatusCode()) {
                    streaming = true;
                    return streamed(response, permit, fileName, forwardedHeaders);
                }
                if (!it.hasNext() || (status != 400 && status != 404)) {
                    return reqourError(id, url, response);
                }
                logger.info("Reqour cannot serve {} for build {} ({}), trying the next URL", url, id, status);
                response.close();
            }
        } finally {
            if (!streaming) {
                permit.release();
            }
        }
    }

    private Response reqourError(String id, String url, Response response) {
        int status = response.getStatus();
        boolean json = response.getMediaType() != null
                && response.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE);
        String retryAfter = response.getHeaderString(HttpHeaders.RETRY_AFTER);
        String body;
        try {
            body = response.readEntity(String.class);
        } finally {
            response.close();
        }
        logger.warn("Reqour cannot serve {} for build {}: {} {}", url, id, status, body);
        boolean busy = status == 503 && json;
        int mapped = status == 400 || status == 404 || status == 502 || status == 504 || busy ? status : 502;
        Object entity = json ? body : new ErrorResponse("REQOUR_ERROR", "Reqour answered HTTP " + status + ".");
        Response.ResponseBuilder error = Response.status(mapped).entity(entity).type(MediaType.APPLICATION_JSON_TYPE);
        if (busy && retryAfter != null) {
            error.header(HttpHeaders.RETRY_AFTER, retryAfter);
        }
        return error.build();
    }

    /**
     * A concurrency permit, released once: when the transfer ends, or by a safety timer when the transfer never starts
     * (which also closes reqour's answer). The timer only runs until the transfer starts, so a long transfer keeps its
     * permit, and it is cancelled on release.
     */
    static final class Permit {
        private final Semaphore permits;
        private final AtomicBoolean released = new AtomicBoolean();
        private volatile Response pending;
        private final ScheduledFuture<?> safety;

        Permit(Semaphore permits) {
            this(permits, PERMIT_SAFETY_RELEASE_MINUTES, TimeUnit.MINUTES);
        }

        Permit(Semaphore permits, long safetyDelay, TimeUnit unit) {
            this.permits = permits;
            this.safety = PERMIT_RELEASER.schedule(this::abandon, safetyDelay, unit);
        }

        /** Reqour's answer, waiting for the container to write it. */
        void pending(Response response) {
            pending = response;
        }

        /** False when the safety timer already gave the permit back: the transfer must not start. */
        boolean start() {
            return safety.cancel(false);
        }

        void release() {
            safety.cancel(false);
            if (released.compareAndSet(false, true)) {
                permits.release();
            }
        }

        private void abandon() {
            Response response = pending;
            if (response != null) {
                logger.warn("The SCM content response was never written, closing reqour's answer");
                response.close();
            }
            release();
        }
    }

    private static Response streamed(Response response, Permit permit, String fileName, String... forwardedHeaders) {
        permit.pending(response);
        StreamingOutput body = out -> {
            try {
                if (!permit.start()) {
                    throw new IOException("The SCM content response was abandoned");
                }
                try (InputStream in = response.readEntity(InputStream.class)) {
                    in.transferTo(out);
                }
            } finally {
                response.close();
                permit.release();
            }
        };
        Response.ResponseBuilder builder = Response.ok(body, response.getMediaType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        for (String header : forwardedHeaders) {
            String value = response.getHeaderString(header);
            if (value != null) {
                builder.header(header, value);
            }
        }
        return builder.build();
    }

    @Override
    @RolesAllowed({ USERS, USERS_ADMIN })
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

    @Override
    public Page<Attachment> getAttachments(String id, PageParameters pageParameters) {
        return attachmentProvider.getAttachmentsForBuild(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ(),
                id);
    }

    @Override
    @RolesAllowed({ USERS_BUILD_ADMIN, USERS_ADMIN })
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
    @RolesAllowed({ USERS, USERS_ADMIN })
    public void addAttribute(String id, String key, String value) {
        provider.addAttribute(id, key, value);
    }

    @Override
    @RolesAllowed({ USERS, USERS_ADMIN })
    public void removeAttribute(String id, String key) {
        provider.removeAttribute(id, key);
    }

    @Override
    public BuildPushReport getPushResult(String buildId) {
        throw new BrewNotSupportedException();
    }

    @Override
    public Page<BuildPushOperation> getPushOperations(String buildId, PageParameters pageParameters) {
        throw new BrewNotSupportedException();
    }

    @Override
    @RolesAllowed({ USERS, USERS_ADMIN })
    public BuildPushOperation push(String id, BuildPushParameters buildPushParameters) {
        throw new BrewNotSupportedException();
    }

    @Override
    @RolesAllowed({ USERS, USERS_ADMIN })
    public void cancelPush(String id) {
        throw new BrewNotSupportedException();
    }

    @Override
    @RolesAllowed({ USERS, USERS_ADMIN })
    public void completePush(String id, BuildPushCompleted buildPushCompleted) {
        throw new BrewNotSupportedException();
    }

    @Override
    public BuildConfigurationRevision getBuildConfigRevision(String id) {
        return provider.getBuildConfigurationRevision(id);
    }

    @Override
    @RolesAllowed({ USERS, USERS_ADMIN })
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
                createBifrostRedirectURL(id, "alignment-log"));
    }

    @Override
    public StreamingOutput getBuildLogs(String id) {
        throw new RedirectionException(Response.Status.TEMPORARY_REDIRECT, createBifrostRedirectURL(id, "build-log"));
    }

    private URI createBifrostRedirectURL(String buildID, String logType) {
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
