/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.endpoints;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.requests.CreateAndSyncSCMRequest;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.RepositoryCreationResponse;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.facade.providers.api.BuildConfigurationProvider;
import org.jboss.pnc.facade.providers.api.SCMRepositoryProvider;
import org.jboss.pnc.facade.validation.ValidationBuilder;
import org.jboss.pnc.rest.api.endpoints.SCMRepositoryEndpoint;
import org.jboss.pnc.rest.api.parameters.PageParameters;

@ApplicationScoped
public class SCMRepositoryEndpointImpl implements SCMRepositoryEndpoint {

    @Context
    private HttpServletResponse servletResponse;

    @Inject
    private SCMRepositoryProvider scmRepositoryProvider;

    @Inject
    private BuildConfigurationProvider buildConfigurationProvider;

    private EndpointHelper<Integer, SCMRepository, SCMRepository> endpointHelper;

    @PostConstruct
    public void init() {
        endpointHelper = new EndpointHelper<>(SCMRepository.class, scmRepositoryProvider);
    }

    @Override
    public Page<SCMRepository> getAll(PageParameters pageParameters, String matchUrl, String searchUrl) {

        return scmRepositoryProvider.getAllWithMatchAndSearchUrl(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ(),
                matchUrl,
                searchUrl);
    }

    @Override
    public SCMRepository getSpecific(String id) {
        return endpointHelper.getSpecific(id);
    }

    @Override
    public void update(String id, SCMRepository repositoryConfiguration) {
        endpointHelper.update(id, repositoryConfiguration);
    }

    @Override
    public SCMRepository patchSpecific(String id, SCMRepository scmRepository) {
        return endpointHelper.update(id, scmRepository);
    }

    @Override
    public RepositoryCreationResponse createNew(CreateAndSyncSCMRequest request) {
        ValidationBuilder.validateObject(request, WhenCreatingNew.class)
                .validateNotEmptyArgument()
                .validateAnnotations();

        RepositoryCreationResponse responseDTO = scmRepositoryProvider
                .createSCMRepository(request.getScmUrl(), request.getPreBuildSyncEnabled());

        if (responseDTO.getRepository() == null) {
            // not in database, it is being created, return 202
            servletResponse.setStatus(Response.Status.ACCEPTED.getStatusCode());
        } else {
            // created, return 201
            servletResponse.setStatus(Response.Status.CREATED.getStatusCode());
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
    public Page<BuildConfiguration> getBuildConfigs(String id, PageParameters pageParameters) {
        return buildConfigurationProvider.getBuildConfigurationsForScmRepository(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ(),
                id);
    }
}
