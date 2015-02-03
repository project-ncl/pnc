package org.jboss.pnc.rest.endpoint;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.jboss.pnc.rest.provider.ProjectProvider;
import org.jboss.pnc.rest.restmodel.ProjectRest;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(value = "/project", description = "Project related information")
@Path("/project")
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
    public Response getAll(@ApiParam(value = "Page index", required = false) @QueryParam("pageIndex") Integer pageIndex,
            @ApiParam(value = "Pagination size", required = false) @QueryParam("pageSize") Integer pageSize,
            @ApiParam(value = "Sorting field", required = false) @QueryParam("sorted_by") String field,
            @ApiParam(value = "Sort direction", required = false) @QueryParam("sorting") String sorting) {
        return Response.ok(projectProvider.getAll(pageIndex, pageSize, field, sorting)).build();
    }

    @ApiOperation(value = "Gets specific Project")
    @GET
    @Path("/{id}")
    public ProjectRest getSpecific(@ApiParam(value = "Project id", required = true) @PathParam("id") Integer id) {
        return projectProvider.getSpecific(id);
    }
}
