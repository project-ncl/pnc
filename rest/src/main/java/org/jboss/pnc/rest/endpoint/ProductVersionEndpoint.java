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
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.provider.BuildConfigurationSetProvider;
import org.jboss.pnc.rest.provider.ConflictedEntryException;
import org.jboss.pnc.rest.provider.ProductVersionProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.restmodel.ProductVersionRest;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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

    @ApiOperation(value = "Gets all Product Versions", responseContainer = "List", response = ProductVersionRest.class)
    @GET
    public Response getAll(@ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query") @QueryParam("q") String rsql){
        return super.getAll(pageIndex, pageSize, sortingRsql, rsql);
    }

    @ApiOperation(value = "Gets specific Product Version", response = ProductVersionRest.class)
    @GET
    @Path("/{id}")
    public Response getSpecific(
            @ApiParam(value = "Product Version id", required = true) @PathParam("id") Integer id) {
        return super.getSpecific(id);
    }

    @ApiOperation(value = "Updates an existing Product Version")
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "Product Version id", required = true) @PathParam("id") Integer id,
            @NotNull @Valid ProductVersionRest productVersionRest) throws ConflictedEntryException {
        return super.update(id, productVersionRest);
    }

    @ApiOperation(value = "Gets build configuration sets associated with a product version",
            responseContainer = "List", response = BuildConfigurationSetRest.class)
    @GET
    @Path("/{id}/build-configuration-sets")
    public Response getBuildConfigurationSets(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query") @QueryParam("q") String rsql,
            @ApiParam(value = "Product Version id", required = true) @PathParam("id") Integer id) {
        return Response.ok().entity(buildConfigurationSetProvider.getAllForProductVersion(pageIndex, pageSize, sortingRsql,
                rsql, id)).build();
    }

    @ApiOperation(value = "Create a new ProductVersion for a Product", response = ProductVersionRest.class)
    @POST
    public Response createNewProductVersion(@NotNull @Valid ProductVersionRest productVersionRest, @Context UriInfo uriInfo)
            throws ConflictedEntryException {
        return super.createNew(productVersionRest, uriInfo);
    }

}
