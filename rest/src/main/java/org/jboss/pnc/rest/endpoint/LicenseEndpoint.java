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

import org.jboss.pnc.rest.provider.LicenseProvider;
import org.jboss.pnc.rest.restmodel.LicenseRest;
import org.jboss.pnc.rest.utils.Utility;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.util.List;

/**
 * Created by avibelli on Feb 5, 2015
 *
 */
@Api(value = "/licenses", description = "License related information")
@Path("/licenses")
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

    @ApiOperation(value = "Gets all Licenses", responseContainer = "List", response = LicenseRest.class)
    @GET
    public List<LicenseRest> getAll(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        return licenseProvider.getAll(pageIndex, pageSize, sortingRsql, rsql);
    }

    @ApiOperation(value = "Get specific License", response = LicenseRest.class)
    @GET
    @Path("/{id}")
    public Response getSpecific(@ApiParam(value = "License id", required = true) @PathParam("id") Integer id) {
        return Utility.createRestEnityResponse(licenseProvider.getSpecific(id), id);
    }

    @ApiOperation(value = "Creates a new License", response = LicenseRest.class)
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
        licenseProvider.update(licenseId, licenseRest);
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
