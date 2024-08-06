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

import org.jboss.pnc.api.constants.BuildConfigurationParameterKeys;
import org.jboss.pnc.common.json.moduleconfig.AlignmentConfig;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.BuildConfigurationRef;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildConfigurationWithLatestBuild;
import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.requests.BuildConfigWithSCMRequest;
import org.jboss.pnc.dto.response.AlignmentParameters;
import org.jboss.pnc.dto.response.BuildConfigCreationResponse;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.Parameter;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.enums.BuildCategory;
import org.jboss.pnc.facade.BuildTriggerer;
import org.jboss.pnc.facade.providers.api.BuildConfigurationProvider;
import org.jboss.pnc.facade.providers.api.BuildConfigurationSupportedGenericParametersProvider;
import org.jboss.pnc.facade.providers.api.BuildPageInfo;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.facade.providers.api.GroupConfigurationProvider;
import org.jboss.pnc.facade.validation.AlreadyRunningException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.facade.validation.InvalidRequestException;
import org.jboss.pnc.facade.validation.ValidationBuilder;
import org.jboss.pnc.rest.api.endpoints.BuildConfigurationEndpoint;
import org.jboss.pnc.rest.api.parameters.BuildParameters;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.BuildRequestException;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Context
    private HttpServletResponse servletResponse;

    @Inject
    private AlignmentConfig alignmentConfig;

    private EndpointHelper<Integer, BuildConfiguration, BuildConfigurationRef> endpointHelper;

    private static final int MAX_ALIGNMENT_PARAMETERS_LENGTH = 8192;

    @PostConstruct
    public void init() {
        endpointHelper = new EndpointHelper<>(BuildConfiguration.class, buildConfigurationProvider);
    }

    @Override
    public Page<BuildConfiguration> getAll(PageParameters pageParams) {
        return endpointHelper.getAll(pageParams);
    }

    private void validate(BuildConfiguration buildConfiguration) {
        if (buildConfiguration != null) {
            Map<String, String> parameters = buildConfiguration.getParameters();

            if (parameters != null) {
                validateParameters(parameters);
            }
        }
    }

    private void validateParameters(Map<String, String> parameters) {
        String buildCategoryKey = BuildConfigurationParameterKeys.BUILD_CATEGORY.name();
        if (parameters.containsKey(buildCategoryKey)) {
            String buildCategoryStr = parameters.get(buildCategoryKey);
            try {
                BuildCategory.valueOf(buildCategoryStr.toUpperCase());
            } catch (Exception ex) {
                List<String> categories = Stream.of(BuildCategory.values())
                        .map(BuildCategory::name)
                        .collect(Collectors.toList());
                throw new InvalidEntityException(
                        buildCategoryKey + " value '" + buildCategoryStr + "' is invalid. Valid values are: "
                                + String.join(", ", categories) + '.');
            }
        }

        String alignmentParametersKey = BuildConfigurationParameterKeys.ALIGNMENT_PARAMETERS.name();
        if (parameters.containsKey(alignmentParametersKey)) {
            String alignmentParametersStr = parameters.get(alignmentParametersKey);

            if (alignmentParametersStr.length() > MAX_ALIGNMENT_PARAMETERS_LENGTH) {
                throw new InvalidEntityException(
                        alignmentParametersKey + " value is too long. Maximum length is: "
                                + MAX_ALIGNMENT_PARAMETERS_LENGTH + ". Consider using asterisk (*).");
            }
        }
    }

    @Override
    public Page<BuildConfigurationWithLatestBuild> getAllWithLatestBuild(PageParameters pageParams) {
        return buildConfigurationProvider.getBuildConfigurationIncludeLatestBuild(
                pageParams.getPageIndex(),
                pageParams.getPageSize(),
                pageParams.getSort(),
                pageParams.getQ());
    }

    @Override
    public BuildConfiguration createNew(BuildConfiguration buildConfiguration) {
        validate(buildConfiguration);
        return endpointHelper.create(buildConfiguration);
    }

    @Override
    public BuildConfiguration getSpecific(String id) {
        return endpointHelper.getSpecific(id);
    }

    @Override
    public void update(String id, BuildConfiguration buildConfiguration) {
        validate(buildConfiguration);
        endpointHelper.update(id, buildConfiguration);
    }

    @Override
    public BuildConfiguration patchSpecific(String id, BuildConfiguration buildConfiguration) {
        validate(buildConfiguration);
        return endpointHelper.update(id, buildConfiguration);
    }

    @Override
    @Transactional
    public Build trigger(String id, BuildParameters buildParams) {
        try {
            return triggerBuild(id, OptionalInt.empty(), buildParams);
        } catch (BuildConflictException ex) {
            throw new AlreadyRunningException(ex, ex.getBuildTaskId());
        } catch (BuildRequestException ex) {
            throw new InvalidRequestException(ex);
        }
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
    public Page<BuildConfiguration> getDependants(String id, PageParameters pageParams) {
        return buildConfigurationProvider.getDependants(
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

        return buildConfigurationProvider.getRevisions(pageParams.getPageIndex(), pageParams.getPageSize(), id);
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
        try {
            return triggerBuild(id, OptionalInt.of(rev), buildParams);
        } catch (BuildConflictException ex) {
            throw new AlreadyRunningException(ex, ex.getBuildTaskId());
        } catch (BuildRequestException ex) {
            throw new InvalidRequestException(ex);
        }
    }

    @Override
    public BuildConfiguration restoreRevision(String id, int rev) {
        return buildConfigurationProvider.restoreRevision(id, rev)
                .orElseThrow(
                        () -> new NotFoundException(
                                "BuildConfigurationAudited with [id=" + id + ", rev=" + rev + "] does not exists"));
    }

    @Override
    public BuildConfigCreationResponse createWithSCM(BuildConfigWithSCMRequest request) {
        ValidationBuilder.validateObject(request, WhenCreatingNew.class)
                .validateNotEmptyArgument()
                .validateAnnotations();

        BuildConfigCreationResponse responseDTO = buildConfigurationProvider.createWithScm(request);

        if (responseDTO.getTaskId() == null) {
            // created, return 201
            servletResponse.setStatus(Response.Status.CREATED.getStatusCode());
        } else {
            // not in database, it is being created, return 202
            servletResponse.setStatus(Response.Status.ACCEPTED.getStatusCode());
        }
        servletResponse.setContentType(MediaType.APPLICATION_JSON);

        try {
            servletResponse.flushBuffer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return responseDTO;
    }

    @Override
    public Set<Parameter> getSupportedParameters() {
        return bcSupportedGenericParametersProvider.getSupportedGenericParameters();
    }

    @Override
    public AlignmentParameters getBuildTypeDefaultAlignmentParameters(String buildType) {
        return new AlignmentParameters(buildType, alignmentConfig.getAlignmentParameters().get(buildType));
    }

    private Build triggerBuild(String id, OptionalInt rev, BuildParameters buildParams)
            throws BuildConflictException, BuildRequestException {
        try {
            logger.debug(
                    "Endpoint /build requested for buildConfigurationId: {}, revision: {}, parameters: {}",
                    id,
                    rev,
                    buildParams);

            BuildOptions buildOptions = toBuildOptions(buildParams);
            String buildId = buildTriggerer.triggerBuild(Integer.parseInt(id), rev, buildOptions);

            return buildProvider.getSpecific(buildId);
        } catch (CoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    private BuildOptions toBuildOptions(BuildParameters buildParams) {
        BuildOptions buildOptions = new BuildOptions(
                buildParams.isTemporaryBuild(),
                buildParams.isBuildDependencies(),
                buildParams.isKeepPodOnFailure(),
                false,
                buildParams.getRebuildMode(),
                buildParams.getAlignmentPreference());
        checkBuildOptionsValidity(buildOptions);
        return buildOptions;
    }

    public static void checkBuildOptionsValidity(BuildOptions buildOptions) {
        if (!buildOptions.isTemporaryBuild() && buildOptions.isTimestampAlignment()) {
            // Combination timestampAlignment + standard build is not allowed
            throw new InvalidEntityException(
                    "Combination of the build parameters is not allowed. Timestamp alignment is allowed only for temporary builds. ");
        }
    }

}
