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

import java.util.Arrays;
import java.util.List;

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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.jboss.pnc.model.ProductRelease.SupportLevel;
import org.jboss.pnc.rest.provider.ProductReleaseProvider;
import org.jboss.pnc.rest.provider.ProjectProvider;
import org.jboss.pnc.rest.restmodel.ProductReleaseRest;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Api(value = "/product-releases", description = "Product Release related information")
@Path("/product-releases")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductReleaseEndpoint {

    private ProductReleaseProvider productReleaseProvider;

    public ProductReleaseEndpoint() {
    }

    @Inject
    public ProductReleaseEndpoint(ProductReleaseProvider productReleaseProvider, ProjectProvider projectProvider) {
        this.productReleaseProvider = productReleaseProvider;
    }

    @ApiOperation(value = "Gets all Product Releases")
    @GET
    public List<ProductReleaseRest> getAll(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query") @QueryParam("q") String rsql) {
        return productReleaseProvider.getAll(pageIndex, pageSize, sortingRsql, rsql);
    }

    @ApiOperation(value = "Gets all Product Releases of the Specified Product Version")
    @GET
    @Path("/product-versions/{versionId}")
    public List<ProductReleaseRest> getAllByProductVersionId(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql,
            @ApiParam(value = "Product Version id", required = true) @PathParam("versionId") Integer versionId) {

        return productReleaseProvider.getAllForProductVersion(pageIndex, pageSize, sortingRsql, rsql, versionId);
    }

    @ApiOperation(value = "Gets specific Product Release")
    @GET
    @Path("/{id}")
    public ProductReleaseRest getSpecific(@ApiParam(value = "Product Release id", required = true) @PathParam("id") Integer id) {
        return productReleaseProvider.getSpecific(id);
    }

    @ApiOperation(value = "Creates a new Product Release")
    @POST
    @Path("/product-versions/{versionId}")
    public Response createNew(
            @ApiParam(value = "Product Version id", required = true) @PathParam("versionId") Integer versionId,
            @NotNull @Valid ProductReleaseRest productReleaseRest, @Context UriInfo uriInfo) {

        int id = productReleaseProvider.store(versionId, productReleaseRest);
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getRequestUri()).path("{id}");
        return Response.created(uriBuilder.build(id)).entity(productReleaseProvider.getSpecific(id)).build();
    }

    @ApiOperation(value = "Updates an existing Product Release")
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "Product Release id", required = true) @PathParam("id") Integer id,
            @NotNull @Valid ProductReleaseRest productReleaseRest, @Context UriInfo uriInfo) {
        productReleaseProvider.update(id, productReleaseRest);
        return Response.ok().build();
    }

    @ApiOperation(value = "Gets all Product Releases Support Level")
    @GET
    @Path("/support-level")
    public List<SupportLevel> getAllSupportLevel () {
        return Arrays.asList(SupportLevel.values());
    }

}
