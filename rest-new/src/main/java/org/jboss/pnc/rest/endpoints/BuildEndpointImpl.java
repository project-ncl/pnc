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

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.util.StringUtils;
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
import org.jboss.pnc.facade.providers.api.ArtifactProvider;
import org.jboss.pnc.facade.providers.api.BuildPageInfo;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.rest.api.endpoints.BuildEndpoint;
import org.jboss.pnc.rest.api.parameters.BuildAttributeParameters;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class BuildEndpointImpl implements BuildEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static BuildPageInfo toBuildPageInfo(PageParameters page, BuildsFilterParameters builds) {
        return new BuildPageInfo(page.getPageIndex(), page.getPageSize(), page.getSort(), page.getSort(), builds.isLatest(), builds.isRunning());
    }

    @Inject
    private BuildProvider provider;

    @Inject
    private ArtifactProvider artifactProvider;

    private EndpointHelper<Build, BuildRef> endpointHelper;

    @Inject
    private Configuration configuration;

    private String pncRestBaseUrl;

    @PostConstruct
    public void init() {

        endpointHelper = new EndpointHelper<>(Build.class, provider);

        try {
            String pncBaseUrl = StringUtils.stripEndingSlash(configuration.getGlobalConfig().getPncUrl());
            pncRestBaseUrl = StringUtils.stripEndingSlash(pncBaseUrl);
        } catch (ConfigurationParseException e) {
            logger.error("There is a problem while parsing system configuration. Using defaults.", e);
        }
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
    public Build getSpecific(int id) {
        return endpointHelper.getSpecific(id);
    }

    @Override
    public void delete(int id) {
        endpointHelper.delete(id);
    }

    @Override
    public void update(int id, Build build) {
        endpointHelper.update(id, build);
    }

    @Override
    public Page<Build> getAll(PageParameters pageParams, BuildsFilterParameters filterParams, BuildAttributeParameters attributes) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Page<Artifact> getBuiltArtifacts(int id, PageParameters pageParameters) {
        return artifactProvider.getBuiltArtifactsForBuild(pageParameters.getPageIndex(),
                                                  pageParameters.getPageSize(),
                                                  pageParameters.getSort(),
                                                  pageParameters.getQ(),
                                                  id);
    }

    @Override
    public void setBuiltArtifacts(int id, List<Integer> artifactIds) {
        provider.setBuiltArtifacts(id, artifactIds);
    }

    @Override
    public Page<Artifact> getDependencyArtifacts(int id, PageParameters pageParameters) {
        return artifactProvider.getDependantArtifactsForBuild(pageParameters.getPageIndex(),
                                                      pageParameters.getPageSize(),
                                                      pageParameters.getSort(),
                                                      pageParameters.getQ(),
                                                      id);
    }

    @Override
    public void setDependentArtifacts(int id, List<Integer> artifactIds) {
        provider.setDependentArtifacts(id, artifactIds);
    }

    @Override
    public Response getInternalScmArchiveLink(int id) {
        URI toRedirect = provider.getInternalScmArchiveLink(id);

        if (toRedirect == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.temporaryRedirect(toRedirect).build();
        }
    }

    @Override
    public void addAttribute(int id, String key, String value) {
        provider.addAttribute(id, key, value);
    }

    @Override
    public void removeAttribute(int id, String key) {
        provider.removeAttribute(id, key);
    }

    @Override
    public BuildPushResult getPushResult(int id) {
        return provider.getBrewPushResult(id);
    }

    @Override
    public Page<BuildPushResult> push(BuildPushRequest buildPushRequest) {

        try {
            return provider.brewPush(buildPushRequest, getCompleteCallbackUrl());
        } catch (ProcessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cancelPush(int id) {
        provider.brewPushCancel(id);
    }

    @Override
    public BuildPushResult completePush(int id, BuildPushResult buildPushResult) {

        try {
            return provider.brewPushComplete(id, buildPushResult);
        } catch (ProcessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BuildConfigurationRevision getBuildConfigurationRevision(int id) {
        return provider.getBuildConfigurationRevision(id);
    }

    @Override
    public void cancel(int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Graph<Build> getDependencyGraph(int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getAlignLogs(int id) {
        return provider.getRepourLog(id);
    }

    @Override
    public String getBuildLogs(int id) {
        return provider.getBuildLog(id);
    }

    @Override
    public SSHCredentials getSshCredentials(int id) {
        return provider.getSshCredentials(id);
    }

    private String getCompleteCallbackUrl() {
        // TODO: can we improve this?
        return pncRestBaseUrl + "/builds/%d/brew-push/complete";
    }
}
