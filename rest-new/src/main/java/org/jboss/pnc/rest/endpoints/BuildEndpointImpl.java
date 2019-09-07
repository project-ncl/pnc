/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.common.logging.BuildTaskContext;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.dto.requests.BuildPushRequest;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.SSHCredentials;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.facade.BrewPusher;
import org.jboss.pnc.facade.BuildTriggerer;
import org.jboss.pnc.facade.providers.api.ArtifactProvider;
import org.jboss.pnc.facade.providers.api.BuildPageInfo;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.rest.api.endpoints.BuildEndpoint;
import org.jboss.pnc.rest.api.parameters.BuildAttributeParameters;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@ApplicationScoped
public class BuildEndpointImpl implements BuildEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static BuildPageInfo toBuildPageInfo(PageParameters page, BuildsFilterParameters builds) {
        return new BuildPageInfo(page.getPageIndex(), page.getPageSize(), page.getSort(), page.getQ(), builds.isLatest(), builds.isRunning());
    }

    @Inject
    private BuildProvider provider;

    @Inject
    private ArtifactProvider artifactProvider;

    @Inject
    private BuildTriggerer buildTriggerer;

    @Inject
    private BrewPusher brewPusher;

    private EndpointHelper<Integer, Build, BuildRef> endpointHelper;

    @PostConstruct
    public void init() {
        endpointHelper = new EndpointHelper<>(Build.class, provider);
    }

    @Override
    public Page<Build> getAllByStatusAndLogContaining(BuildStatus status, String search, PageParameters pageParameters) {
        return provider.getAllByStatusAndLogContaining(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ(),
                status,
                search);
    }

    @Override
    public Build getSpecific(String id) {
        return endpointHelper.getSpecific(id);
    }

    @Override
    public void delete(String id) {
        endpointHelper.delete(id);
    }

    @Override
    public void update(String id, Build build) {
        endpointHelper.update(id, build);
    }

    @Override
    public Page<Build> getAll(PageParameters pageParams, BuildsFilterParameters filterParams, BuildAttributeParameters attributes) {
        return provider.getBuilds(toBuildPageInfo(pageParams, filterParams));
    }

    @Override
    public Page<Artifact> getBuiltArtifacts(String id, PageParameters pageParameters) {
        return artifactProvider.getBuiltArtifactsForBuild(pageParameters.getPageIndex(),
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
    public Page<Artifact> getDependencyArtifacts(String id, PageParameters pageParameters) {
        return artifactProvider.getDependantArtifactsForBuild(pageParameters.getPageIndex(),
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
    public BuildPushResult getPushResult(String id) {
        return brewPusher.getBrewPushResult(Integer.parseInt(id));
    }

    @Override
    public BuildPushResult push(BuildPushRequest buildPushRequest) {

        try {
            return brewPusher.brewPush(buildPushRequest);
        } catch (ProcessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cancelPush(String id) {
        brewPusher.brewPushCancel(Integer.parseInt(id));
    }

    @Override
    public BuildPushResult completePush(String id, BuildPushResult buildPushResult) {

        try {
            return brewPusher.brewPushComplete(Integer.parseInt(id), buildPushResult);
        } catch (ProcessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BuildConfigurationRevision getBuildConfigurationRevision(String id) {
        return provider.getBuildConfigurationRevision(id);
    }

    @Override
    public void cancel(String buildId) {
        try {
            logger.debug("Received cancel request for buildTaskId: {}.", buildId);

            Optional<BuildTaskContext> mdcMeta = buildTriggerer.getMdcMeta(Integer.parseInt(buildId));
            if (mdcMeta.isPresent()) {
                MDCUtils.addContext(mdcMeta.get());
            } else {
                logger.warn("Unable to retrieve MDC meta. There is no running build for buildTaskId: {}.", buildId);
            }

            if (!buildTriggerer.cancelBuild(Integer.parseInt(buildId))) {
                throw new NotFoundException();
            }
        } catch (CoreException e) {
            logger.error("Unable to cancel the build [" + buildId + "].", e);
            throw new RuntimeException("Unable to cancel the build [" + buildId + "].");
        }

        logger.debug("Cancel request for buildTaskId {} successfully processed.", buildId);
    }

    @Override
    public Graph<Build> getDependencyGraph(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getAlignLogs(String id) {
        return provider.getRepourLog(id);
    }

    @Override
    public String getBuildLogs(String id) {
        return provider.getBuildLog(id);
    }

    @Override
    public SSHCredentials getSshCredentials(String id) {
        return provider.getSshCredentials(id);
    }
}
