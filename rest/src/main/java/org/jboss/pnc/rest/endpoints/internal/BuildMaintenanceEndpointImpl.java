/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.endpoints.internal;

import org.jboss.pnc.rest.endpoints.internal.api.BuildMaintenanceEndpoint;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

@ApplicationScoped
public class BuildMaintenanceEndpointImpl implements BuildMaintenanceEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(BuildMaintenanceEndpointImpl.class);

    @Inject
    private RepositoryManager repositoryManager;

    @Override
    public Response collectRepoManagerResult(Long id) {
        logger.info("Getting repository manager result for build record id {}.", id);
        RepositoryManagerResult result;
        try {
            result = repositoryManager.collectRepoManagerResult(id);
        } catch (RepositoryManagerException ex) {
            logger.error("Error when collecting repository manager result for build record " + id, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.toString()).build();
        }
        if (result == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(result).build();
    }
}
