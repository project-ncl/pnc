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

import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.dto.requests.BuildPushRequest;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.SSHCredentials;
import org.jboss.pnc.facade.providers.api.ArtifactProvider;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.facade.providers.api.Provider;
import org.jboss.pnc.rest.api.endpoints.BuildEndpoint;
import org.jboss.pnc.rest.api.parameters.BuildAttributeParameters;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class BuildEndpointImpl extends AbstractEndpoint<Build, BuildRef> implements BuildEndpoint {

    @Inject
    private BuildProvider buildProvider;

    @Inject
    private ArtifactProvider artifactProvider;

    public BuildEndpointImpl() {
        super(Build.class);
    }

    @Override
    protected Provider<?, Build, BuildRef> provider() {
        return buildProvider;
    }

    @Override
    public Page<Build> getAll(PageParameters pageParams, BuildsFilterParameters filterParams, BuildAttributeParameters attributes) {
        throw new UnsupportedOperationException("Not implemented yet");
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
    public Page<Artifact> getDependencyArtifacts(int id, PageParameters pageParameters) {
        return artifactProvider.getDependantArtifactsForBuild(pageParameters.getPageIndex(),
                                                              pageParameters.getPageSize(),
                                                              pageParameters.getSort(),
                                                              pageParameters.getQ(),
                                                              id);
    }

    @Override
    public void addAttribute(int id, String key, String value) {
        buildProvider.addAttribute(id, key, value);
    }

    @Override
    public void removeAttribute(int id, String key) {
        buildProvider.removeAttribute(id, key);
    }

    @Override
    public BuildPushResult getPushResult(int id) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public Page<BuildPushResult> push(BuildPushRequest buildRecordPushRequest) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public void cancelPush(int id) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public BuildPushResult completePush(int id, BuildPushResult buildRecordPushResult) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public BuildConfigurationRevision getBuildConfigurationRevision(int id) {
        return buildProvider.getBuildConfigurationRevision(id);
    }

    @Override
    public void cancel(int id) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public Graph<Build> getDependencyGraph(int id) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public String getAlignLogs(int id) {
        return buildProvider.getRepourLog(id);
    }

    @Override
    public String getBuildLogs(int id) {
        return buildProvider.getBuildLog(id);
    }

    @Override
    public SSHCredentials getSshCredentials(int id) {
        return buildProvider.getSshCredentials(id);
    }

    @Override
    public Build getSpecific(int id) {
        // TODO: need to also take care of *running* builds
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public void delete(int id) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
