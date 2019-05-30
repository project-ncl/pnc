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
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.Parameter;
import org.jboss.pnc.dto.response.BuildConfigCreationResponse;
import org.jboss.pnc.facade.providers.api.BuildConfigurationProvider;
import org.jboss.pnc.facade.providers.api.BuildConfigurationSupportedGenericParametersProvider;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.facade.providers.api.GroupConfigurationProvider;
import org.jboss.pnc.rest.api.endpoints.BuildConfigurationEndpoint;
import org.jboss.pnc.rest.api.parameters.BuildParameters;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import org.jboss.pnc.facade.BuildTriggerer;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class BuildConfigurationEndpointImpl extends AbstractEndpoint<BuildConfiguration, BuildConfigurationRef> implements BuildConfigurationEndpoint {

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

    public BuildConfigurationEndpointImpl() {
        super(BuildConfiguration.class);
    }

    @Override
    protected BuildConfigurationProvider provider() {
        return buildConfigurationProvider;
    }

    @Override
    public Page<BuildConfiguration> getAll(PageParameters pageParams) {
        return super.getAll(pageParams);
    }

    @Override
    public BuildConfiguration createNew(BuildConfiguration buildConfiguration) {
        return super.create(buildConfiguration);
    }

    @Override
    public BuildConfiguration getSpecific(int id) {
        return super.getSpecific(id);
    }

    @Override
    public void update(int id, BuildConfiguration buildConfiguration) {
        super.update(id, buildConfiguration);
    }

    @Override
    public void deleteSpecific(int id) {
        super.delete(id);
    }

    @Override
    public Build trigger(int id, BuildParameters buildParams) {
        return triggerBuild(id, OptionalInt.empty(), buildParams);
    }

    @Override
    public Page<Build> getBuilds(int id, PageParameters pageParams, BuildsFilterParameters buildsFilter) {
        // TODO: handle buildsFilter
        return buildProvider.getBuildsForBuildConfiguration(
                pageParams.getPageIndex(),
                pageParams.getPageSize(),
                pageParams.getSort(),
                pageParams.getQ(),
                id);
    }

    @Override
    public BuildConfiguration clone(int id) {
        return buildConfigurationProvider.clone(id);
    }

    @Override
    public Page<GroupConfiguration> getGroupConfigs(int id, PageParameters pageParams) {

        return groupConfigurationProvider.getGroupConfigurationsForBuildConfiguration(
                pageParams.getPageIndex(),
                pageParams.getPageSize(),
                pageParams.getSort(),
                pageParams.getQ(),
                id);
    }

    @Override
    public Page<BuildConfiguration> getDependencies(int id, PageParameters pageParams) {

        return buildConfigurationProvider.getDependencies(
                pageParams.getPageIndex(),
                pageParams.getPageSize(),
                pageParams.getSort(),
                pageParams.getQ(),
                id);
    }

    @Override
    public void addDependency(int id, BuildConfigurationRef dependency) {
        buildConfigurationProvider.addDependency(id, dependency.getId());
    }

    @Override
    public void removeDependency(int id, int dependencyId) {
        buildConfigurationProvider.removeDependency(id, dependencyId);
    }

    @Override
    public Page<BuildConfigurationRevision> getRevisions(int id, PageParameters pageParams) {

        return buildConfigurationProvider.getRevisions(
                pageParams.getPageIndex(),
                pageParams.getPageSize(),
                id);
    }

    @Override
    public BuildConfigurationRevision createRevision(int id, BuildConfiguration buildConfiguration) {
        buildConfigurationProvider.update(id, buildConfiguration);

        Optional<BuildConfigurationRevision> buildConfigurationRevision =
                buildConfigurationProvider.getLatestAuditedMatchingBCRest(buildConfiguration);

        return buildConfigurationRevision
                .orElseThrow(() -> new RuntimeException("Couldn't find updated BuildConfigurationAudited entity. " +
                                                        "BuildConfiguration to be stored: " + buildConfiguration));
    }

    @Override
    public BuildConfigurationRevision getRevision(int id, int rev) {
        return buildConfigurationProvider.getRevision(id, rev);
    }

    @Override
    public Build triggerRevision(int id, int rev, BuildParameters buildParams) {
        return triggerBuild(id, OptionalInt.of(rev), buildParams);
    }

    @Override
    public BuildConfiguration restoreRevision(int id, int rev) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public BuildConfigCreationResponse createWithSCM(BuildConfigWithSCMRequest request) {
        return buildConfigurationProvider.createWithScm(request);
    }

    @Override
    public Set<Parameter> getSupportedParameters() {
        return bcSupportedGenericParametersProvider.getSupportedGenericParameters();
    }

    private Build triggerBuild(int id, OptionalInt rev, BuildParameters buildParams) {
        try {
            logger.debug("Endpoint /build requested for buildConfigurationId: {}, revision: {}, parameters: {}",
                    id, rev, buildParams);

            BuildOptions buildOptions = toBuildOptions(buildParams);
            int buildId = buildTriggerer.triggerBuild(id, rev, buildOptions);

            return buildProvider.getSpecific(buildId);
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
