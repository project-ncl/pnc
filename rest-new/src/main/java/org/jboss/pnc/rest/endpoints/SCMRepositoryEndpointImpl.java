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

import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.requests.CreateAndSyncSCMRequest;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.TaskResponse;
import org.jboss.pnc.facade.providers.api.SCMRepositoryProvider;
import org.jboss.pnc.rest.api.endpoints.SCMRepositoryEndpoint;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.spi.exception.CoreException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class SCMRepositoryEndpointImpl
        extends AbstractEndpoint<SCMRepository, SCMRepository>
        implements SCMRepositoryEndpoint {

    @Inject
    private SCMRepositoryProvider scmRepositoryProvider;

    public SCMRepositoryEndpointImpl() {
        super(SCMRepository.class);
    }

    @Override
    protected SCMRepositoryProvider provider() {
        return scmRepositoryProvider;
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
    public SCMRepository getSpecific(int id) {
        return super.getSpecific(id);
    }

    @Override
    public void update(int id, SCMRepository repositoryConfiguration) {
        super.update(id, repositoryConfiguration);
    }

    @Override
    public TaskResponse createNew(CreateAndSyncSCMRequest request) {
        try{
            return scmRepositoryProvider.createSCMRepositoryWithOneUrl(request,null,null);
        } catch (CoreException e) {
            //FIXME what to do??
            return null;
        }

    }
}
