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
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.rest.provider.ConflictedEntryException;
import org.jboss.pnc.rest.provider.EnvironmentProvider;
import org.jboss.pnc.rest.restmodel.EnvironmentRest;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Api(value = "/environments", description = "Environment related information")
@Path("/environments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EnvironmentEndpoint extends AbstractEndpoint<Environment, EnvironmentRest> {

    public EnvironmentEndpoint() {
    }

    @Inject
    public EnvironmentEndpoint(EnvironmentProvider environmentProvider) {
        super(environmentProvider);
    }

    @ApiOperation(value = "Gets all Environments", responseContainer = "List", response = EnvironmentRest.class)
    @GET
    public Response getAll(@ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        return super.getAll(pageIndex, pageSize, sortingRsql, rsql);
    }

    @ApiOperation(value = "Get specific Environment", response = EnvironmentRest.class)
    @GET
    @Path("/{id}")
    public Response getSpecific(@ApiParam(value = "Environment id", required = true) @PathParam("id") Integer id) {
        return super.getSpecific(id);
    }

    @ApiOperation(value = "Creates a new Environment", response = EnvironmentRest.class)
    @POST
    public Response createNew(@NotNull @Valid EnvironmentRest environmentRest, @Context UriInfo uriInfo)
            throws ConflictedEntryException {
        return super.createNew(environmentRest, uriInfo);
    }

    @ApiOperation(value = "Updates an existing Environment")
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "Environment id", required = true) @PathParam("id") Integer environmentId,
            @NotNull @Valid EnvironmentRest environmentRest, @Context UriInfo uriInfo) throws ConflictedEntryException {
        return super.update(environmentId, environmentRest);
    }

    @ApiOperation(value = "Deletes an existing Environment")
    @DELETE
    @Path("/{id}")
    public Response delete(@ApiParam(value = "Environment id", required = true) @PathParam("id") Integer id) {
        return super.delete(id);
    }
}
