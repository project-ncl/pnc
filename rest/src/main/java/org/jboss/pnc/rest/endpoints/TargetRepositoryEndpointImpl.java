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

import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.TargetRepository;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.ArtifactProvider;
import org.jboss.pnc.facade.providers.api.TargetRepositoryProvider;
import org.jboss.pnc.rest.api.endpoints.TargetRepositoryEndpoint;
import org.jboss.pnc.rest.api.parameters.PageParameters;

@ApplicationScoped
public class TargetRepositoryEndpointImpl implements TargetRepositoryEndpoint {

    @Context
    private HttpServletResponse servletResponse;

    @Inject
    private TargetRepositoryProvider targetRepositoryProvider;

    @Inject
    private ArtifactProvider artifactProvider;

    private EndpointHelper<Integer, TargetRepository, TargetRepository> endpointHelper;

    @PostConstruct
    public void init() {
        endpointHelper = new EndpointHelper<>(TargetRepository.class, targetRepositoryProvider);
    }

    @Override
    public Page<TargetRepository> getAll(PageParameters pageParameters) {

        return targetRepositoryProvider.getAll(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ());
    }

    @Override
    public TargetRepository getSpecific(String id) {
        return endpointHelper.getSpecific(id);
    }

    @Override
    public TargetRepository createNew(TargetRepository targetRepository) {
        return endpointHelper.create(targetRepository);
    }

    @Override
    public Page<Artifact> getArtifacts(Integer id, PageParameters pageParameters) {
        return artifactProvider.getArtifactsForTargetRepository(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ(),
                id);
    }
}
