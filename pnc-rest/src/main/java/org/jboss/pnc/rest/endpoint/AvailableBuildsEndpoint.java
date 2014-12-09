package org.jboss.pnc.rest.endpoint;

import com.wordnik.swagger.annotations.*;
import org.jboss.pnc.rest.mapping.ProjectBuildConfigurationRest;
import org.jboss.pnc.rest.trigger.BuildTriggerProvider;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api(value = "/configuration", description = "Project Build Configuration endpoint")
@Path("/configuration")
@Produces(MediaType.APPLICATION_JSON)
public class AvailableBuildsEndpoint {

    BuildTriggerProvider buildTriggerProvider;

    public AvailableBuildsEndpoint() {
    }

    @Inject
    public AvailableBuildsEndpoint(BuildTriggerProvider buildTriggerProvider) {
        this.buildTriggerProvider = buildTriggerProvider;
    }

    @ApiOperation(value = "Finds list of provisioned Project Build Configurations", responseContainer="List", response = ProjectBuildConfigurationRest.class)
    @GET
    public Response getAvailableBuildForTrigger() {
        List<ProjectBuildConfigurationRest> projectBuildConfigurations = buildTriggerProvider.getAvailableBuildConfigurations();
        return Response.ok().entity(projectBuildConfigurations).build();
    }

    @ApiOperation(value = "Finds provisioned Project Build Configuration", response = ProjectBuildConfigurationRest.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Everything is OK"),
            @ApiResponse(code = 404, message = "Configuration not found")
    })
    @GET
    @Path("{id}")
    public Response getSpecificBuild(
            @ApiParam(value = "Configurations id", required = true)
            @PathParam("id") Integer id) {
        ProjectBuildConfigurationRest projectBuildConfigurations = buildTriggerProvider.getSpecificConfiguration(id);
        if(projectBuildConfigurations == null) {
            return Response.status(404).build();
        }
        return Response.ok().entity(projectBuildConfigurations).build();
    }
}
