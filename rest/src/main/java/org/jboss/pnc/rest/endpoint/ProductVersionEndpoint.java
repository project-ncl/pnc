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

import org.jboss.pnc.rest.provider.ProductVersionProvider;
import org.jboss.pnc.rest.provider.ProjectProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.restmodel.ProductVersionRest;
import org.jboss.pnc.rest.utils.Utility;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.util.List;

@Api(value = "/product-versions", description = "Product Version related information")
@Path("/product-versions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductVersionEndpoint {

    private ProductVersionProvider productVersionProvider;

    public ProductVersionEndpoint() {
    }

    @Inject
    public ProductVersionEndpoint(ProductVersionProvider productVersionProvider, ProjectProvider projectProvider) {
        this.productVersionProvider = productVersionProvider;
    }

    @ApiOperation(value = "Gets all Product Versions")
    @GET
    public List<ProductVersionRest> getAll(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query") @QueryParam("q") String rsql){
        return productVersionProvider.getAll(pageIndex, pageSize, sortingRsql, rsql);
    }

    @ApiOperation(value = "Gets specific Product Version")
    @GET
    @Path("/{id}")
    public Response getSpecific(
            @ApiParam(value = "Product Version id", required = true) @PathParam("id") Integer id) {
        return Utility.createRestEnityResponse(productVersionProvider.getSpecific(id), id);
    }

    @ApiOperation(value = "Updates an existing Product Version")
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "Product Version id", required = true) @PathParam("id") Integer id,
            @NotNull @Valid ProductVersionRest productVersionRest, @Context UriInfo uriInfo) {
        productVersionProvider.update(id, productVersionRest);
        return Response.ok().build();
    }

    @ApiOperation(value = "Gets build configuration sets associated with a product version")
    @GET
    @Path("/{id}/build-configuration-sets")
    public List<BuildConfigurationSetRest> getBuildConfigurationSets(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query") @QueryParam("q") String rsql,
            @ApiParam(value = "Product Version id", required = true) @PathParam("id") Integer id) {
        return productVersionProvider.getBuildConfigurationSets(id);
    }

    @ApiOperation(value = "Create a new ProductVersion for a Product")
    @POST
    public Response createNewProductVersion(@NotNull @Valid ProductVersionRest productVersionRest, @Context UriInfo uriInfo){
        int productVersionId = productVersionProvider.store(productVersionRest);
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getRequestUri()).path("{productVersionId}");
        return Response.created(uriBuilder.build(productVersionId)).entity(productVersionProvider.getSpecific(productVersionId)).build();
    }

}
