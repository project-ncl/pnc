package org.jboss.pnc.rest.endpoint;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.util.List;

@Api(value = "/project/{projectId}/configuration", description = "Build Configuration related information")
@Path("/project/{projectId}/configuration")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildConfigurationEndpoint {

    private BuildConfigurationProvider buildConfigurationProvider;
    private BuildRecordProvider buildRecordProvider;

    public BuildConfigurationEndpoint() {
    }

    @Inject
    public BuildConfigurationEndpoint(BuildConfigurationProvider buildConfigurationProvider,
            BuildRecordProvider buildRecordProvider) {
        this.buildConfigurationProvider = buildConfigurationProvider;
        this.buildRecordProvider = buildRecordProvider;
    }

    @ApiOperation(value = "Gets all Project's Build Configurations")
    @GET
    public List<BuildConfigurationRest> getAll(
            @ApiParam(value = "Project id", required = true) @PathParam("projectId") Integer projectId) {
        return buildConfigurationProvider.getAll(projectId);
    }

    @ApiOperation(value = "Gets a specific Project's Build Configuration")
    @GET
    @Path("/{id}")
    public BuildConfigurationRest getSpecific(
            @ApiParam(value = "Project id", required = true) @PathParam("projectId") Integer projectId,
            @ApiParam(value = "Project's Configuration id", required = true) @PathParam("id") Integer id) {
        return buildConfigurationProvider.getSpecific(projectId, id);
    }

    @ApiOperation(value = "Gets the Build Records of a specific Build Configuration")
    @GET
    @Path("/{id}/result")
    public List<BuildRecordRest> getResultsOfSpecificBuildConfiguration(
            @ApiParam(value = "Project id", required = true) @PathParam("projectId") Integer projectId,
            @ApiParam(value = "Project's Configuration id", required = true) @PathParam("id") Integer id) {
        return buildRecordProvider.getAllArchivedOfBuildConfiguration(id);
    }

    @ApiOperation(value = "Creates new Project's Build Configuration")
    @POST
    public Response createNew(@ApiParam(value = "Project id", required = true) @PathParam("projectId") Integer projectId,
            @NotNull @Valid BuildConfigurationRest buildConfigurationRest, @Context UriInfo uriInfo) {
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getRequestUri()).path("{id}");
        int id = buildConfigurationProvider.store(projectId, buildConfigurationRest);
        return Response.created(uriBuilder.build(id)).build();
    }

    @ApiOperation(value = "Creates new Project's Build Configuration for a Product Version")
    @POST
    @Path("/{id}/version/{versionId}")
    public Response createNew(@ApiParam(value = "Project id", required = true) @PathParam("projectId") Integer projectId,
            @ApiParam(value = "Product Version id", required = true) @PathParam("versionId") Integer versionId,
            @NotNull @Valid BuildConfigurationRest buildConfigurationRest, @Context UriInfo uriInfo) {
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getRequestUri()).path("{id}");
        int id = buildConfigurationProvider.store(projectId, buildConfigurationRest);
        return Response.created(uriBuilder.build(id)).build();
    }

    @ApiOperation(value = "Deletes a Project's Build Configuration")
    @DELETE
    @Path("/{id}")
    public Response delete(@ApiParam(value = "Project id", required = true) @PathParam("projectId") Integer projectId,
            @ApiParam(value = "Project's Configuration id", required = true) @PathParam("id") Integer id) {
        buildConfigurationProvider.delete(id);
        return Response.ok().build();
    }
}
