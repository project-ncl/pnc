package org.jboss.pnc.rest.endpoint;

import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
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

import org.jboss.pnc.rest.provider.ProjectProvider;
import org.jboss.pnc.rest.restmodel.ProjectRest;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

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
    public Response update(@ApiParam(value = "Project id", required = true) @PathParam("id") Integer productId,
            @NotNull @Valid ProjectRest projectRest, @Context UriInfo uriInfo) {
        projectProvider.update(projectRest);
        return Response.ok().build();
    }

    @ApiOperation(value = "Gets all Projects of a Product Version")
    @GET
    @Path("/product/{productId}/version/{versionId}")
    public List<ProjectRest> getAllByProductId(
            @ApiParam(value = "Product id", required = true) @PathParam("productId") Integer productId,
            @ApiParam(value = "Product Version id id", required = true) @PathParam("versionId") Integer versionId) {
        return projectProvider.getAllForProductAndProductVersion(productId, versionId);
    }
}
