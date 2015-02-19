/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
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
import org.jboss.pnc.rest.provider.LicenseProvider;
import org.jboss.pnc.rest.restmodel.LicenseRest;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

/**
 * Created by avibelli on Feb 5, 2015
 *
 */
@Api(value = "/license", description = "License related information")
@Path("/license")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LicenseEndpoint {

    private LicenseProvider licenseProvider;

    public LicenseEndpoint() {
    }

    @Inject
    public LicenseEndpoint(LicenseProvider licenseProvider) {
        this.licenseProvider = licenseProvider;
    }

    @ApiOperation(value = "Gets all Licenses")
    @GET
    public Response getAll(@ApiParam(value = "Page index", required = false) @QueryParam("pageIndex") Integer pageIndex,
            @ApiParam(value = "Pagination size", required = false) @QueryParam("pageSize") Integer pageSize,
            @ApiParam(value = "Sorting field", required = false) @QueryParam("sorted_by") String field,
            @ApiParam(value = "Sort direction", required = false) @QueryParam("sorting") String sorting,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        return Response.ok(licenseProvider.getAll(pageIndex, pageSize, field, sorting, rsql)).build();
    }

    @ApiOperation(value = "Get specific License")
    @GET
    @Path("/{id}")
    public LicenseRest getSpecific(@ApiParam(value = "License id", required = true) @PathParam("id") Integer id) {
        return licenseProvider.getSpecific(id);
    }

    @ApiOperation(value = "Creates a new License")
    @POST
    public Response createNew(@NotNull @Valid LicenseRest licenseRest, @Context UriInfo uriInfo) {
        int id = licenseProvider.store(licenseRest);
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getRequestUri()).path("{id}");
        return Response.created(uriBuilder.build(id)).entity(licenseProvider.getSpecific(id)).build();
    }

    @ApiOperation(value = "Updates an existing License")
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "License id", required = true) @PathParam("id") Integer licenseId,
            @NotNull @Valid LicenseRest licenseRest, @Context UriInfo uriInfo) {
        licenseProvider.update(licenseRest);
        return Response.ok().build();
    }

    @ApiOperation(value = "Deletes an existing License")
    @DELETE
    @Path("/{id}")
    public Response delete(@ApiParam(value = "License id", required = true) @PathParam("id") Integer licenseId) {
        licenseProvider.delete(licenseId);
        return Response.ok().build();
    }
}
