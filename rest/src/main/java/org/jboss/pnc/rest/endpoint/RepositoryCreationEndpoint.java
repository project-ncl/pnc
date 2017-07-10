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
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.RepositoryConfigurationRest;
import org.jboss.pnc.rest.restmodel.bpm.RepositoryCreationRest;
import org.jboss.pnc.rest.restmodel.bpm.RepositoryCreationResultRest;
import org.jboss.pnc.rest.restmodel.response.error.ErrorResponseRest;
import org.jboss.pnc.rest.validation.ValidationBuilder;
import org.jboss.pnc.rest.validation.exceptions.ValidationException;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.RepositoryConfigurationRepository;
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

    private BuildConfigurationRepository buildConfigurationRepository;

    private RepositoryConfigurationRepository repositoryConfigurationRepository;

    @Deprecated //CDI workaround
    public RepositoryCreationEndpoint() {
    }

    @Inject
    public RepositoryCreationEndpoint(
            BuildConfigurationRepository buildConfigurationRepository,
            RepositoryConfigurationRepository repositoryConfigurationRepository) {
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.repositoryConfigurationRepository = repositoryConfigurationRepository;
    }

    @ApiOperation(value = "Store Repository Configuration and Build Configuration. Should be used from service (bpm) that created the internal repository.")
    @ApiResponses(value = {
            @ApiResponse(
                    code = SUCCESS_CODE,
                    message = "Return RepositoryCreationResultRest with buildConfigurationId and repositoryConfigurationId, where buildConfigurationId is -1 if the BuildConfiguration has not been specified in input parameters.",
                    response = RepositoryCreationResultRest.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    public Response createNewRCAndBC(RepositoryCreationRest repositoryCreationRest, @Context UriInfo uriInfo)
            throws ValidationException {

        logger.debug("Creating new RC and BC from: {}", repositoryCreationRest.toString());


        RepositoryConfigurationRest repositoryConfigurationRest = repositoryCreationRest.getRepositoryConfigurationRest();
        ValidationBuilder.validateObject(repositoryConfigurationRest, WhenCreatingNew.class)
                .validateNotEmptyArgument()
                .validateAnnotations();
        RepositoryConfiguration repositoryConfiguration = repositoryConfigurationRest.toDBEntityBuilder().build();
        repositoryConfigurationRepository.save(repositoryConfiguration);

        BuildConfigurationRest buildConfigurationRest = repositoryCreationRest.getBuildConfigurationRest();

        int buildConfigurationSavedId = -1;

        if (buildConfigurationRest != null) {
            ValidationBuilder.validateObject(buildConfigurationRest, WhenCreatingNew.class)
                    .validateNotEmptyArgument()
                    .validateAnnotations();
            BuildConfiguration buildConfiguration = buildConfigurationRest.toDBEntityBuilder()
                    .repositoryConfiguration(repositoryConfiguration)
                    .build();
            BuildConfiguration buildConfigurationSaved = buildConfigurationRepository.save(buildConfiguration);
            buildConfigurationSavedId = buildConfigurationSaved.getId();
        }

        RepositoryCreationResultRest result = new RepositoryCreationResultRest(
                repositoryConfiguration.getId(),
                buildConfigurationSavedId);
        return Response.ok().entity(result).build();
    }

}
