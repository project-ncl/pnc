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
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.provider.BuildConfigurationSetProvider;
import org.jboss.pnc.rest.provider.ProductVersionProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.restmodel.ProductVersionRest;
import org.jboss.pnc.rest.restmodel.response.error.ErrorResponseRest;
import org.jboss.pnc.rest.swagger.response.BuildConfigurationSetPage;
import org.jboss.pnc.rest.swagger.response.ProductVersionPage;
import org.jboss.pnc.rest.swagger.response.ProductVersionSingleton;
import org.jboss.pnc.rest.validation.exceptions.ValidationException;

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

import java.util.List;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_CODE;
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
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SORTING_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SORTING_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

@Api(value = "/product-versions", description = "Product Version related information")
@Path("/product-versions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductVersionEndpoint extends AbstractEndpoint<ProductVersion, ProductVersionRest> {

    private ProductVersionProvider productVersionProvider;
    private BuildConfigurationSetProvider buildConfigurationSetProvider;

    public ProductVersionEndpoint() {
    }

    @Inject
    public ProductVersionEndpoint(ProductVersionProvider productVersionProvider,
            BuildConfigurationSetProvider buildConfigurationSetProvider) {
        super(productVersionProvider);
        this.productVersionProvider = productVersionProvider;
        this.buildConfigurationSetProvider = buildConfigurationSetProvider;
    }

    @ApiOperation(value = "Gets all Product Versions")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = ProductVersionPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = ProductVersionPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    public Response getAll(@ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q){
        return super.getAll(pageIndex, pageSize, sort, q);
    }

    @ApiOperation(value = "Gets specific Product Version")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = ProductVersionSingleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}")
    public Response getSpecific(
            @ApiParam(value = "Product Version id", required = true) @PathParam("id") Integer id) {
        return super.getSpecific(id);
    }

    @ApiOperation(value = "Updates an existing Product Version")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "Product Version id", required = true) @PathParam("id") Integer id,
            ProductVersionRest productVersionRest) throws ValidationException {
        return super.update(id, productVersionRest);
    }

    @ApiOperation(value = "Gets build configuration sets associated with a product version")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildConfigurationSetPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildConfigurationSetPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/build-configuration-sets")
    public Response getBuildConfigurationSets(
            @ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q,
            @ApiParam(value = "Product Version id", required = true) @PathParam("id") Integer id) {
        return Response.ok().entity(buildConfigurationSetProvider.getAllForProductVersion(pageIndex, pageSize, sort,
                q, id)).build();
    }

    @PUT
    @Path("/{id}/build-configuration-sets")
    public Response updateBuildConfigurationSets(@ApiParam(value = "Product Version id", required = true) @PathParam("id") Integer id,
            List<BuildConfigurationSetRest> buildConfigurationSetRests) throws ValidationException {
        productVersionProvider.updateBuildConfigurationSets(id, buildConfigurationSetRests);
        return Response.ok().build();
    }

    @ApiOperation(value = "Create a new ProductVersion for a Product")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = ProductVersionSingleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    public Response createNewProductVersion(ProductVersionRest productVersionRest, @Context UriInfo uriInfo)
            throws ValidationException {
        return super.createNew(productVersionRest, uriInfo);
    }

}
