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
import org.jboss.pnc.model.Product;
import org.jboss.pnc.rest.provider.ProductProvider;
import org.jboss.pnc.rest.provider.ProductVersionProvider;
import org.jboss.pnc.rest.restmodel.ProductRest;
import org.jboss.pnc.rest.restmodel.response.error.ErrorResponseRest;
import org.jboss.pnc.rest.swagger.response.ProductPage;
import org.jboss.pnc.rest.swagger.response.ProductVersionPage;
import org.jboss.pnc.rest.swagger.response.ProductSingleton;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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

@Api(value = "/products", description = "Product related information")
@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductEndpoint extends AbstractEndpoint<Product, ProductRest> {

    private ProductVersionProvider productVersionProvider;

    public ProductEndpoint() {
    }

    @Inject
    public ProductEndpoint(ProductProvider productProvider, ProductVersionProvider productVersionProvider) {
        super(productProvider);
        this.productVersionProvider = productVersionProvider;
    }

    @ApiOperation(value = "Gets all Products")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = ProductPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = ProductPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    public Response getAll(@ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q) {
        return super.getAll(pageIndex, pageSize, sort, q);
    }

    @ApiOperation(value = "Get specific Product")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = ProductSingleton.class),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION, response = ProductSingleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}")
    public Response getSpecific(@ApiParam(value = "Product id", required = true) @PathParam("id") Integer id) {
        return super.getSpecific(id);
    }

    @ApiOperation(value = "Creates a new Product")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = ProductSingleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    public Response createNew(ProductRest productRest, @Context UriInfo uriInfo) throws RestValidationException {
        return super.createNew(productRest, uriInfo);
    }

    @ApiOperation(value = "Updates an existing Product")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "Product id", required = true) @PathParam("id") Integer productId,
            @NotNull @Valid ProductRest productRest, @Context UriInfo uriInfo) throws RestValidationException {
        return super.update(productId, productRest);
    }

    @ApiOperation(value = "Get all versions for a Product")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = ProductVersionPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = ProductVersionPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/product-versions")
    public Response getProductVersions(
            @ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q,
            @ApiParam(value = "Product id", required = true) @PathParam("id") Integer productId) {
        return fromCollection(productVersionProvider.getAllForProduct(pageIndex, pageSize, sort, q, productId));
    }

}
