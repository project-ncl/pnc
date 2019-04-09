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

import java.lang.invoke.MethodHandles;
import javax.enterprise.context.ApplicationScoped;

import javax.inject.Inject;

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.dto.requests.BuildPushRequest;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.SSHCredentials;
import org.jboss.pnc.facade.providers.api.ArtifactProvider;
import org.jboss.pnc.facade.providers.api.BuildPageInfo;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.rest.api.endpoints.BuildEndpoint;
import org.jboss.pnc.rest.api.parameters.BuildAttributeParameters;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class BuildEndpointImpl extends AbstractEndpoint<Build, BuildRef> implements BuildEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static BuildPageInfo toBuildPageInfo(PageParameters page, BuildsFilterParameters builds) {
        return new BuildPageInfo(page.getPageIndex(), page.getPageSize(), page.getSort(), page.getSort(), builds.isLatest(), builds.isRunning());
    }

    @Inject
    private BuildProvider provider;

    @Inject
    private ArtifactProvider artifactProvider;

    public BuildEndpointImpl() {
        super(Build.class);
    }

    @Override
    protected BuildProvider provider() {
        return provider;
    }

    @Override
    public Build getSpecific(int id) {
        return super.getSpecific(id);
    }

    @Override
    public void delete(int id) {
        super.delete(id);
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
    public Page<Artifact> getDependencyArtifacts(int id, PageParameters pageParameters) {
        return artifactProvider.getDependantArtifactsForBuild(pageParameters.getPageIndex(),
                                                      pageParameters.getPageSize(),
                                                      pageParameters.getSort(),
                                                      pageParameters.getQ(),
                                                      id);
    }

    @Override
    public String getInternalScmArchiveLink(int id) {
        return provider.getInternalScmArchiveLink(id);
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Page<BuildPushResult> push(BuildPushRequest buildRecordPushRequest) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cancelPush(int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BuildPushResult completePush(int id, BuildPushResult buildRecordPushResult) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

}
