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

import org.jboss.pnc.rest.provider.ProjectProvider;
import org.jboss.pnc.rest.restmodel.ProjectRest;
import org.jboss.pnc.rest.utils.Utility;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.util.List;

@Api(value = "/projects", description = "Project related information")
@Path("/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProjectEndpoint {

    private ProjectProvider projectProvider;

    public ProjectEndpoint() {
    }

    @Inject
    public ProjectEndpoint(ProjectProvider projectProvider) {
        this.projectProvider = projectProvider;
    }

    @ApiOperation(value = "Gets all Projects")
    @GET
    public List<ProjectRest> getAll(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query") @QueryParam("q") String rsql) {
        return projectProvider.getAll(pageIndex, pageSize, sortingRsql, rsql);
    }

    @ApiOperation(value = "Gets specific Project")
    @GET
    @Path("/{id}")
    public Response getSpecific(@ApiParam(value = "Project id", required = true) @PathParam("id") Integer id) {
        return Utility.createRestEnityResponse(projectProvider.getSpecific(id), id);
    }

    @ApiOperation(value = "Creates a new Project")
    @POST
    public Response createNew(@NotNull @Valid ProjectRest projectRest, @Context UriInfo uriInfo) {
        int id = projectProvider.store(projectRest);
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getRequestUri()).path("{id}");
        return Response.created(uriBuilder.build(id)).entity(projectProvider.getSpecific(id)).build();
    }

    @ApiOperation(value = "Updates an existing Project")
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "Project id", required = true) @PathParam("id") Integer id,
            @NotNull @Valid ProjectRest projectRest, @Context UriInfo uriInfo) {
        projectProvider.update(id, projectRest);
        return Response.ok().build();
    }

    @ApiOperation(value = "Removes a specific project and associated build configurations")
    @DELETE
    @Path("/{id}")
    public Response deleteSpecific(@ApiParam(value = "Project id", required = true) @PathParam("id") Integer id) {
        projectProvider.delete(id);
        return Response.ok().build();
    }

}
