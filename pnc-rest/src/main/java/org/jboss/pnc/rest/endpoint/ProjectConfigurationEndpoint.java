package org.jboss.pnc.rest.endpoint;

import com.wordnik.swagger.annotations.*;
import org.jboss.pnc.rest.mapping.ProjectBuildConfigurationRest;
import org.jboss.pnc.rest.trigger.BuildConfigurationProvider;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

@Api(value = "/configuration", description = "Project Build Configuration endpoint")
@Path("/configuration")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProjectConfigurationEndpoint {

    BuildConfigurationProvider buildConfigurationProvider;

    public ProjectConfigurationEndpoint() {
    }

    @Inject
    public ProjectConfigurationEndpoint(BuildConfigurationProvider buildConfigurationProvider) {
        this.buildConfigurationProvider = buildConfigurationProvider;
    }

    @ApiOperation(value = "Finds list of provisioned Project Build Configurations", responseContainer="List", response = ProjectBuildConfigurationRest.class)
    @GET
    public Response getAvailableBuildForTrigger() {
        List<ProjectBuildConfigurationRest> projectBuildConfigurations = buildConfigurationProvider.getAvailableBuildConfigurations();
        return Response.ok().entity(projectBuildConfigurations).build();
    }

    @ApiOperation(value = "Stores provided configuration")
    @POST
    public Response addNewConfiguration(ProjectBuildConfigurationRest configuration, @Context UriInfo uriInfo) {
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getRequestUri()).path("{id}");
        int configurationId = buildConfigurationProvider.storeConfiguration(configuration);
        return Response.created(uriBuilder.build(configurationId)).entity(configurationId).build();
    }

    @ApiOperation(value = "Updates specific configuration")
    @PUT
    @Path("{id}")
    public Response updateConfiguration(
            @ApiParam(value = "Configuration id", required = true)
            @PathParam("id") Integer id,
            ProjectBuildConfigurationRest configuration) {
        buildConfigurationProvider.updateConfiguration(configuration);
        return Response.ok().build();
    }

    @ApiOperation(value = "Finds provisioned Project Build Configuration", response = ProjectBuildConfigurationRest.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Everything is OK"),
            @ApiResponse(code = 404, message = "Configuration not found")
    })
    @GET
    @Path("{id}")
    public Response getSpecificBuild(
            @ApiParam(value = "Configuration id", required = true)
            @PathParam("id") Integer id) {
        ProjectBuildConfigurationRest projectBuildConfigurations = buildConfigurationProvider.getSpecificConfiguration(id);
        if(projectBuildConfigurations == null) {
            return Response.status(404).build();
        }
        return Response.ok().entity(projectBuildConfigurations).build();
    }
}
