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
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.rest.provider.RepositoryConfigurationProvider;
import org.jboss.pnc.rest.restmodel.RepositoryConfigurationRest;
import org.jboss.pnc.rest.restmodel.response.error.ErrorResponseRest;
import org.jboss.pnc.rest.swagger.response.RepositoryConfigurationPage;
import org.jboss.pnc.rest.swagger.response.RepositoryConfigurationSingleton;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.lang.invoke.MethodHandles;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_CREATED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_CREATED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NO_CONTENT_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NO_CONTENT_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.QUERY_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.QUERY_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SEARCH_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SORTING_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SORTING_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

/**
 * @author Jakub Bartecek
 */
@Api(value = "/repository-configurations", description = "Repository configuration entities")
@Path("/repository-configurations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RepositoryConfigurationEndpoint extends AbstractEndpoint<RepositoryConfiguration, RepositoryConfigurationRest> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    private RepositoryConfigurationProvider repositoryConfigurationProvider;

    public RepositoryConfigurationEndpoint() {
    }

    @Inject
    public RepositoryConfigurationEndpoint(RepositoryConfigurationProvider repositoryConfigurationProvider) {
        super(repositoryConfigurationProvider);
        this.repositoryConfigurationProvider = repositoryConfigurationProvider;
    }


    @ApiOperation(value = "Gets all Repository Configurations")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = RepositoryConfigurationPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = RepositoryConfigurationPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    public Response getAll(@ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
                           @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
                           @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
                           @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q) {
        return fromCollection(repositoryConfigurationProvider.getAll(pageIndex, pageSize, sort, q));
    }

    @ApiOperation(value = "Creates a new Repository Configuration.")
    @ApiResponses(value = {
            @ApiResponse(code = ENTITY_CREATED_CODE, message = ENTITY_CREATED_DESCRIPTION, response = RepositoryConfigurationSingleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    public Response createNew(RepositoryConfigurationRest repositoryConfigurationRest, @Context UriInfo uriInfo)
            throws RestValidationException {
        repositoryConfigurationRest.validate();
        return super.createNew(repositoryConfigurationRest, uriInfo);
    }

    @ApiOperation(value = "Gets a specific Repository Configuration")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = RepositoryConfigurationSingleton.class),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION, response = RepositoryConfigurationSingleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}")
    public Response getSpecific(
            @ApiParam(value = "Repository Configuration id", required = true) @PathParam("id") Integer id) {
        return super.getSpecific(id);
    }

    @ApiOperation(value = "Updates an existing Repository Configuration")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "Repository Configuration id", required = true) @PathParam("id") Integer id,
                           RepositoryConfigurationRest repositoryConfigurationRest) throws RestValidationException {
        repositoryConfigurationRest.validate();
        return super.update(id, repositoryConfigurationRest);
    }

    @ApiOperation(value = "Search for Repository Configurations based on internal or external url, ignoring the protocol and \".git\" suffix. " +
            "The matching is done using LIKE.")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = RepositoryConfigurationPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = RepositoryConfigurationPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/search-by-scm-url")
    public Response search(@ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = "Url part to search for", required = true) @QueryParam(SEARCH_QUERY_PARAM) String scmUrl) {
        return fromCollection(repositoryConfigurationProvider.searchByScmUrl(pageIndex, pageSize, sort, scmUrl));
    }


    @ApiOperation(value = "Searches for Repository Configurations based on internal or external url, ignoring the protocol and \".git\" suffix. " +
            "Only exact matches are returned.")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = RepositoryConfigurationPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = RepositoryConfigurationPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/match-by-scm-url")
    public Response match(@ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
                           @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
                           @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
                           @ApiParam(value = "Url to search for", required = true) @QueryParam(SEARCH_QUERY_PARAM) String scmUrl) {
        return fromCollection(repositoryConfigurationProvider.matchByScmUrl(pageIndex, pageSize, sort, scmUrl));
    }

}
