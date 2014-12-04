package org.jboss.pnc.rest.endpoint;

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

@Path("/configuration")
public class AvailableBuildsEndpoint {

    BuildTriggerProvider buildTriggerProvider;

    public AvailableBuildsEndpoint() {
    }

    @Inject
    public AvailableBuildsEndpoint(BuildTriggerProvider buildTriggerProvider) {
        this.buildTriggerProvider = buildTriggerProvider;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAvailableBuildForTrigger() {
        List<ProjectBuildConfigurationRest> projectBuildConfigurations = buildTriggerProvider.getAvailableBuildConfigurations();
        return Response.ok().entity(projectBuildConfigurations).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public Response getSpecificBuild(@PathParam("id") Integer id) {
        ProjectBuildConfigurationRest projectBuildConfigurations = buildTriggerProvider.getSpecificConfiguration(id);
        if(projectBuildConfigurations == null) {
            return Response.status(404).build();
        }
        return Response.ok().entity(projectBuildConfigurations).build();
    }
}
