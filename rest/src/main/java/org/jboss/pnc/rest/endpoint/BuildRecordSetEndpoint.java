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
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.rest.provider.BuildRecordSetProvider;
import org.jboss.pnc.rest.restmodel.BuildRecordSetRest;
import org.jboss.pnc.rest.validation.exceptions.ValidationException;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

@Api(value = "/build-record-sets", description = "BuildRecordSet collection")
@Path("/build-record-sets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildRecordSetEndpoint extends AbstractEndpoint<BuildRecordSet, BuildRecordSetRest> {

    private BuildRecordSetProvider buildRecordSetProvider;

    public BuildRecordSetEndpoint() {
    }

    @Inject
    public BuildRecordSetEndpoint(BuildRecordSetProvider buildRecordSetProvider) {
        super(buildRecordSetProvider);
        this.buildRecordSetProvider = buildRecordSetProvider;
    }

    @ApiOperation(value = "Gets all BuildRecordSets",
            responseContainer = "List", response = BuildRecordSetRest.class)
    @GET
    public Response getAll(@ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        return super.getAll(pageIndex, pageSize, sortingRsql, rsql);
    }

    @ApiOperation(value = "Gets a specific BuildRecordSet", response = BuildRecordSetRest.class)
    @GET
    @Path("/{id}")
    public Response getSpecific(@ApiParam(value = "BuildRecordSet id", required = true) @PathParam("id") Integer id) {
        return super.getSpecific(id);
    }

    @ApiOperation(value = "Gets all BuildRecordSet of a Product Version",
            responseContainer = "List", response = BuildRecordSetRest.class)
    @GET
    @Path("/product-milestones/{versionId}")
    public Response getAllForProductMilestone(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql,
            @ApiParam(value = "Product Version id", required = true) @PathParam("versionId") Integer versionId) {
        return fromCollection(
                buildRecordSetProvider.getAllForPerformedInProductMilestone(pageIndex, pageSize, sortingRsql, rsql, versionId));
    }

    @ApiOperation(value = "Gets all BuildRecordSet of a BuildRecord",
            responseContainer = "List", response = BuildRecordSetRest.class)
    @GET
    @Path("/build-records/{recordId}")
    public Response getAllForBuildRecord(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql,
            @ApiParam(value = "BuildRecord id", required = true) @PathParam("recordId") Integer recordId) {
        return fromCollection(buildRecordSetProvider.getAllForBuildRecord(pageIndex, pageSize, sortingRsql, rsql, recordId));
    }

    @ApiOperation(value = "Creates a new BuildRecordSet", response = BuildRecordSetRest.class)
    @POST
    public Response createNew(@NotNull @Valid BuildRecordSetRest buildRecordSetRest, @Context UriInfo uriInfo)
            throws ValidationException {
        return super.createNew(buildRecordSetRest, uriInfo);
    }

    @ApiOperation(value = "Updates an existing BuildRecordSet")
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "BuildRecordSet id", required = true) @PathParam("id") Integer id,
            @NotNull @Valid BuildRecordSetRest buildRecordSetRest) throws ValidationException {
        return super.update(id, buildRecordSetRest);
    }

    @ApiOperation(value = "Deletes a specific BuildRecordSet")
    @DELETE
    @Path("/{id}")
    public Response deleteSpecific(@ApiParam(value = "BuildRecordSet id", required = true) @PathParam("id") Integer id)
            throws ValidationException {
        return super.delete(id);
    }

}
