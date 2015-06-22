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

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import org.jboss.pnc.rest.provider.ProductProvider;
import org.jboss.pnc.rest.provider.ProductVersionProvider;
import org.jboss.pnc.rest.restmodel.ProductRest;
import org.jboss.pnc.rest.restmodel.ProductVersionRest;
import org.jboss.pnc.rest.utils.Utility;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.util.List;

@Api(value = "/products", description = "Product related information")
@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductEndpoint {

    private ProductProvider productProvider;

    private ProductVersionProvider productVersionProvider;

    public ProductEndpoint() {
    }

    @Inject
    public ProductEndpoint(ProductProvider productProvider, ProductVersionProvider productVersionProvider) {
        this.productProvider = productProvider;
        this.productVersionProvider = productVersionProvider;
    }

    @ApiOperation(value = "Gets all Products")
    @GET
    public List<ProductRest> getAll(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        return productProvider.getAll(pageIndex, pageSize, sortingRsql, rsql);
    }

    @ApiOperation(value = "Get specific Product")
    @GET
    @Path("/{id}")
    public Response getSpecific(@ApiParam(value = "Product id", required = true) @PathParam("id") Integer id) {
        return Utility.createRestEnityResponse(productProvider.getSpecific(id), id);
    }

    @ApiOperation(value = "Creates a new Product")
    @POST
    public Response createNew(@NotNull @Valid ProductRest productRest,
            @Context UriInfo uriInfo) {
        int id = productProvider.store(productRest);
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getRequestUri()).path("{id}");
        return Response.created(uriBuilder.build(id)).entity(productProvider.getSpecific(id)).build();
    }

    @ApiOperation(value = "Updates an existing Product")
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "Product id", required = true) @PathParam("id") Integer productId,
            @NotNull @Valid ProductRest productRest, @Context UriInfo uriInfo) {
        productProvider.update(productId, productRest);
        return Response.ok().build();
    }

    @ApiOperation(value = "Get all versions for a Product")
    @GET
    @Path("/{id}/product-versions")
    public List<ProductVersionRest> getProductVersions(@ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
                                          @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
                                          @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
                                          @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql,
                                          @ApiParam(value = "Product id", required = true) @PathParam("id") Integer productId) {
        return productVersionProvider.getAllForProduct(pageIndex, pageSize, sortingRsql, rsql, productId);
    }

    @ApiOperation(value = "Create a new ProductVersion for a Product")
    @POST
    @Path("/{id}/product-versions")
    public Response createNewProductVersion(@ApiParam(value = "Product id", required=true) @PathParam("id") Integer productId,
                                            @NotNull @Valid ProductVersionRest productVersionRest, @Context UriInfo uriInfo){
        int productVersionId = productVersionProvider.store(productId, productVersionRest);
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getRequestUri()).path("{productVersionId}");
        return Response.created(uriBuilder.build(productVersionId)).entity(productVersionProvider.getSpecific(productVersionId)).build();
    }

}
