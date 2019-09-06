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

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.BuildConfigurationRef;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.requests.BuildConfigWithSCMRequest;
import org.jboss.pnc.dto.response.BuildConfigCreationResponse;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.Parameter;
import org.jboss.pnc.facade.BuildTriggerer;
import org.jboss.pnc.facade.providers.api.BuildConfigurationProvider;
import org.jboss.pnc.facade.providers.api.BuildConfigurationSupportedGenericParametersProvider;
import org.jboss.pnc.facade.providers.api.BuildPageInfo;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.facade.providers.api.GroupConfigurationProvider;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.rest.api.endpoints.BuildConfigurationEndpoint;
import org.jboss.pnc.rest.api.parameters.BuildParameters;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.util.OptionalInt;
import java.util.Set;

@ApplicationScoped
public class BuildConfigurationEndpointImpl implements BuildConfigurationEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(BuildConfigurationEndpointImpl.class);

    @Inject
    private BuildConfigurationProvider buildConfigurationProvider;

    @Inject
    private BuildProvider buildProvider;

    @Inject
    private BuildConfigurationSupportedGenericParametersProvider bcSupportedGenericParametersProvider;

    @Inject
    private GroupConfigurationProvider groupConfigurationProvider;

    @Inject
    private BuildTriggerer buildTriggerer;

    private EndpointHelper<BuildConfiguration, BuildConfigurationRef> endpointHelper;

    @PostConstruct
    public void init() {
        endpointHelper = new EndpointHelper<>(BuildConfiguration.class, buildConfigurationProvider);
    }

    @Override
    public Page<BuildConfiguration> getAll(PageParameters pageParams) {
        return endpointHelper.getAll(pageParams);
    }

    @Override
    public BuildConfiguration createNew(BuildConfiguration buildConfiguration) {
//TODO ID
        return endpointHelper.create(buildConfiguration);
    }

    @Override
    public BuildConfiguration getSpecific(String id) {
        return endpointHelper.getSpecific(id);
    }

    @Override
    public void update(String id, BuildConfiguration buildConfiguration) {
        endpointHelper.update(id, buildConfiguration);
    }

    @Override
    public BuildConfiguration patchSpecific(String id, BuildConfiguration buildConfiguration) {
        return endpointHelper.update(id, buildConfiguration);
    }

    @Override
    public void deleteSpecific(String id) {
        endpointHelper.delete(id);
    }

    @Override
    public Build trigger(String id, BuildParameters buildParams) {
        return triggerBuild(id, OptionalInt.empty(), buildParams);
    }

    @Override
    public Page<Build> getBuilds(String id, PageParameters page, BuildsFilterParameters filter) {
        BuildPageInfo pageInfo = BuildEndpointImpl.toBuildPageInfo(page, filter);
        return buildProvider.getBuildsForBuildConfiguration(pageInfo, id);
    }

    @Override
    public BuildConfiguration clone(String id) {
        return buildConfigurationProvider.clone(id);
    }

    @Override
    public Page<GroupConfiguration> getGroupConfigs(String id, PageParameters pageParams) {
        return groupConfigurationProvider.getGroupConfigurationsForBuildConfiguration(
                pageParams.getPageIndex(),
                pageParams.getPageSize(),
                pageParams.getSort(),
                pageParams.getQ(),
                id);
    }

    @Override
    public Page<BuildConfiguration> getDependencies(String id, PageParameters pageParams) {
        return buildConfigurationProvider.getDependencies(
                pageParams.getPageIndex(),
                pageParams.getPageSize(),
                pageParams.getSort(),
                pageParams.getQ(),
                id);
    }

    @Override
    public void addDependency(String id, BuildConfigurationRef dependency) {
        buildConfigurationProvider.addDependency(id, dependency.getId());
    }

    @Override
    public void removeDependency(String id, String dependencyId) {
        buildConfigurationProvider.removeDependency(id, dependencyId);
    }

    @Override
    public Page<BuildConfigurationRevision> getRevisions(String id, PageParameters pageParams) {

        return buildConfigurationProvider.getRevisions(
                pageParams.getPageIndex(),
                pageParams.getPageSize(),
                id);
    }

    @Override
    public BuildConfigurationRevision createRevision(String id, BuildConfiguration buildConfiguration) {
        return buildConfigurationProvider.createRevision(id, buildConfiguration);
    }

    @Override
    public BuildConfigurationRevision getRevision(String id, int rev) {
        return buildConfigurationProvider.getRevision(id, rev);
    }

    @Override
    public Build triggerRevision(String id, int rev, BuildParameters buildParams) {
        return triggerBuild(id, OptionalInt.of(rev), buildParams);
    }

    @Override
    public BuildConfiguration restoreRevision(String id, int rev) {
        return buildConfigurationProvider.restoreRevision(id, rev).orElseThrow(
                () -> new NotFoundException("BuildConfigurationAudited with [id=" + id + ", rev=" + rev + "] does not exists"));
    }

    @Override
    public BuildConfigCreationResponse createWithSCM(BuildConfigWithSCMRequest request) {
        return buildConfigurationProvider.createWithScm(request);
    }

    @Override
    public Set<Parameter> getSupportedParameters() {
        return bcSupportedGenericParametersProvider.getSupportedGenericParameters();
    }

    private Build triggerBuild(String id, OptionalInt rev, BuildParameters buildParams) {
        try {
            logger.debug("Endpoint /build requested for buildConfigurationId: {}, revision: {}, parameters: {}",
                    id, rev, buildParams);

            BuildOptions buildOptions = toBuildOptions(buildParams);
            int buildId = buildTriggerer.triggerBuild(Integer.parseInt(id), rev, buildOptions);

            return buildProvider.getSpecific(Integer.toString(buildId));
        } catch (BuildConflictException | CoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    private BuildOptions toBuildOptions(BuildParameters buildParams) {
        BuildOptions buildOptions = new BuildOptions(
                buildParams.isTemporaryBuild(),
                buildParams.isBuildDependencies(),
                buildParams.isKeepPodOnFailure(),
                buildParams.isTimestampAlignment(),
                buildParams.getRebuildMode());
        checkBuildOptionsValidity(buildOptions);
        return buildOptions;
    }

    public static void checkBuildOptionsValidity(BuildOptions buildOptions) {
        if(!buildOptions.isTemporaryBuild() && buildOptions.isTimestampAlignment()) {
            // Combination timestampAlignment + standard build is not allowed
            throw new InvalidEntityException("Combination of the build parameters is not allowed. Timestamp alignment is allowed only for temporary builds. ");
        }
    }

}
