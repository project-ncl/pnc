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
import org.jboss.pnc.model.Project;
import org.jboss.pnc.rest.provider.ConflictedEntryException;
import org.jboss.pnc.rest.provider.ProjectProvider;
import org.jboss.pnc.rest.restmodel.ProjectRest;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Api(value = "/projects", description = "Project related information")
@Path("/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProjectEndpoint extends AbstractEndpoint<Project, ProjectRest> {

    public ProjectEndpoint() {
    }

    @Inject
    public ProjectEndpoint(ProjectProvider projectProvider) {
        super(projectProvider);
    }

    @ApiOperation(value = "Gets all Projects", responseContainer = "List", response = ProjectRest.class)
    @GET
    public Response getAll(@ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query") @QueryParam("q") String rsql) {
        return super.getAll(pageIndex, pageSize, sortingRsql, rsql);
    }

    @ApiOperation(value = "Gets specific Project", response = ProjectRest.class)
    @GET
    @Path("/{id}")
    public Response getSpecific(@ApiParam(value = "Project id", required = true) @PathParam("id") Integer id) {
        return super.getSpecific(id);
    }

    @ApiOperation(value = "Creates a new Project", response = ProjectRest.class)
    @POST
    public Response createNew(@NotNull @Valid ProjectRest projectRest, @Context UriInfo uriInfo)
            throws ConflictedEntryException {
        return super.createNew(projectRest, uriInfo);
    }

    @ApiOperation(value = "Updates an existing Project")
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "Project id", required = true) @PathParam("id") Integer id,
            @NotNull @Valid ProjectRest projectRest, @Context UriInfo uriInfo) throws ConflictedEntryException {
        return super.update(id, projectRest);
    }

    @ApiOperation(value = "Removes a specific project and associated build configurations")
    @DELETE
    @Path("/{id}")
    public Response deleteSpecific(@ApiParam(value = "Project id", required = true) @PathParam("id") Integer id) {
        return super.delete(id);
    }

}
