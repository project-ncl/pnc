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

import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.TargetRepository;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.ArtifactProvider;
import org.jboss.pnc.facade.providers.api.TargetRepositoryProvider;
import org.jboss.pnc.rest.api.endpoints.TargetRepositoryEndpoint;
import org.jboss.pnc.rest.api.parameters.PageParameters;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;

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
