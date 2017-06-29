/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.provider.RepositoryConfigurationProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.RepositoryConfigurationRest;
import org.jboss.pnc.rest.restmodel.bpm.RepositoryCreationRest;
import org.jboss.pnc.rest.restmodel.bpm.RepositoryCreationResultRest;
import org.jboss.pnc.rest.restmodel.response.error.ErrorResponseRest;
import org.jboss.pnc.rest.swagger.response.ProjectSingleton;
import org.jboss.pnc.rest.validation.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

/**
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>.
 */
@Api(value = "/repository-creation", description = "Endpoints to support repository creation process.")
@Path("/repository-creation")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RepositoryCreationEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryCreationEndpoint.class);

    private BuildConfigurationProvider buildConfigurationProvider;

    private RepositoryConfigurationProvider repositoryConfigurationProvider;

    @Deprecated //CDI workaround
    public RepositoryCreationEndpoint() {
    }

    @Inject
    public RepositoryCreationEndpoint(
            BuildConfigurationProvider buildConfigurationProvider,
            RepositoryConfigurationProvider repositoryConfigurationProvider) {
        this.buildConfigurationProvider = buildConfigurationProvider;
        this.repositoryConfigurationProvider = repositoryConfigurationProvider;
    }

    @ApiOperation(value = "Store Repository Configuration and Build Configuration.")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = ProjectSingleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    public Response createNewRCAndBC(RepositoryCreationRest repositoryCreationRest, @Context UriInfo uriInfo)
            throws ValidationException {

        BuildConfigurationRest buildConfigurationRest = repositoryCreationRest.getBuildConfigurationRest();
        RepositoryConfigurationRest repositoryConfigurationRest = repositoryCreationRest.getRepositoryConfigurationRest();

        Integer repositoryId = repositoryConfigurationProvider.store(repositoryConfigurationRest);
        Integer buildConfigurationId = buildConfigurationProvider.store(buildConfigurationRest);

        RepositoryCreationResultRest result = new RepositoryCreationResultRest(repositoryId, buildConfigurationId);
        return Response.ok().entity(result).build();
    }

}
