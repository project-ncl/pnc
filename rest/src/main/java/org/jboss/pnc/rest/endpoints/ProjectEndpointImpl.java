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

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.Project;
import org.jboss.pnc.dto.ProjectRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.BuildConfigurationProvider;
import org.jboss.pnc.facade.providers.api.BuildPageInfo;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.facade.providers.api.ProjectProvider;
import org.jboss.pnc.rest.api.endpoints.ProjectEndpoint;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ProjectEndpointImpl implements ProjectEndpoint {

    @Inject
    private ProjectProvider projectProvider;

    @Inject
    private BuildConfigurationProvider buildConfigurationProvider;

    @Inject
    private BuildProvider buildProvider;

    private EndpointHelper<Integer, Project, ProjectRef> endpointHelper;

    @PostConstruct
    public void init() {
        endpointHelper = new EndpointHelper<>(Project.class, projectProvider);
    }

    @Override
    public Page<Project> getAll(PageParameters pageParameters) {
        return endpointHelper.getAll(pageParameters);
    }

    @Override
    public Project createNew(Project project) {
        return endpointHelper.create(project);
    }

    @Override
    public Project getSpecific(String id) {
        return endpointHelper.getSpecific(id);
    }

    @Override
    public void update(String id, Project project) {
        endpointHelper.update(id, project);
    }

    @Override
    public Project patchSpecific(String id, Project project) {
        return endpointHelper.update(id, project);
    }

    @Override
    public Page<BuildConfiguration> getBuildConfigurations(String id, PageParameters pageParameters) {
        return buildConfigurationProvider.getBuildConfigurationsForProject(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ(),
                id);
    }

    @Override
    public Page<Build> getBuilds(String id, PageParameters page, BuildsFilterParameters filter) {
        BuildPageInfo pageInfo = BuildPageInfo.toBuildPageInfo(page, filter);
        return buildProvider.getBuildsForProject(pageInfo, id);
    }
}
