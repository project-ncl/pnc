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

import org.jboss.pnc.rest.provider.BuildRecordSetProvider;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.restmodel.BuildRecordSetRest;
import org.jboss.pnc.rest.utils.Utility;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.util.List;

@Api(value = "/build-record-sets", description = "BuildRecordSet collection")
@Path("/build-record-sets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildRecordSetEndpoint {

    private BuildRecordSetProvider buildRecordSetProvider;

    public BuildRecordSetEndpoint() {
    }

    @Inject
    public BuildRecordSetEndpoint(BuildRecordSetProvider buildRecordSetProvider) {
        this.buildRecordSetProvider = buildRecordSetProvider;
    }

    @ApiOperation(value = "Gets all BuildRecordSets")
    @GET
    public List<BuildRecordSetRest> getAll(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        return buildRecordSetProvider.getAll(pageIndex, pageSize, sortingRsql, rsql);
    }

    @ApiOperation(value = "Gets a specific BuildRecordSet")
    @GET
    @Path("/{id}")
    public Response getSpecific(@ApiParam(value = "BuildRecordSet id", required = true) @PathParam("id") Integer id) {
        return Utility.createRestEnityResponse(buildRecordSetProvider.getSpecific(id), id);
    }

    @ApiOperation(value = "Gets all BuildRecordSet of a Product Version")
    @GET
    @Path("/product-milestones/{versionId}")
    public List<BuildRecordSetRest> getAllForProductMilestone(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql,
            @ApiParam(value = "Product Version id", required = true) @PathParam("versionId") Integer versionId) {
        return buildRecordSetProvider.getAllForPerformedInProductMilestone(pageIndex, pageSize, sortingRsql, rsql, versionId);
    }

    @ApiOperation(value = "Gets all BuildRecordSet of a BuildRecord")
    @GET
    @Path("/build-records/{recordId}")
    public List<BuildRecordSetRest> getAllForBuildRecord(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql,
            @ApiParam(value = "BuildRecord id", required = true) @PathParam("recordId") Integer recordId) {
        return buildRecordSetProvider.getAllForBuildRecord(pageIndex, pageSize, sortingRsql, rsql, recordId);
    }

    @ApiOperation(value = "Creates a new BuildRecordSet")
    @POST
    public Response createNew(@NotNull @Valid BuildRecordSetRest buildRecordSetRest, @Context UriInfo uriInfo) {
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getRequestUri()).path("{id}");
        int id = buildRecordSetProvider.store(buildRecordSetRest);
        return Response.created(uriBuilder.build(id)).entity(buildRecordSetProvider.getSpecific(id)).build();
    }

    @ApiOperation(value = "Updates an existing BuildRecordSet")
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "BuildRecordSet id", required = true) @PathParam("id") Integer id,
            @NotNull @Valid BuildRecordSetRest buildRecordSetRest, @Context UriInfo uriInfo) {
        buildRecordSetRest.setId(id);
        buildRecordSetProvider.update(id, buildRecordSetRest);
        return Response.ok().build();
    }

    @ApiOperation(value = "Deletes a specific BuildRecordSet")
    @DELETE
    @Path("/{id}")
    public Response deleteSpecific(@ApiParam(value = "BuildRecordSet id", required = true) @PathParam("id") Integer id) {
        buildRecordSetProvider.delete(id);
        return Response.ok().build();
    }

}
