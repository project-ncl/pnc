package org.jboss.pnc.rest.endpoint;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.rest.provider.ProjectConfigurationProvider;
import org.jboss.pnc.rest.restmodel.ProjectBuildConfigurationRest;
import org.jboss.pnc.rest.trigger.BuildTriggerer;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.List;

@Api(value = "/configuration", description = "Legacy endpoint - please use the new one (starts with Product)")
@Path("/configuration")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LegacyEndpoint {

    private ProjectConfigurationProvider projectConfigurationProvider;
    private BuildTriggerer buildTriggerer;

    public LegacyEndpoint() {
    }

    @Inject
    public LegacyEndpoint(ProjectConfigurationProvider projectConfigurationProvider, BuildTriggerer buildTriggerer) {
        this.projectConfigurationProvider = projectConfigurationProvider;
        this.buildTriggerer = buildTriggerer;
    }

    @ApiOperation(value = "Gets all Product Configuration")
    @GET
    public List<ProjectBuildConfigurationRest> getAll() {
        return projectConfigurationProvider.getAll();
    }

    @ApiOperation(value = "Triggers a build")
    @POST
    @Path("{id}/build")
    public Response build(
            @ApiParam(value = "Project's Configuration id", required = true) @PathParam("id") Integer id,
            @Context UriInfo uriInfo) {
        try {
            Integer runningBuildId = buildTriggerer.triggerBuilds(id);
            UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path("/result/running/{id}");
            URI uri = uriBuilder.build(runningBuildId);
            return Response.created(uri).entity(uri).build();
        } catch (CoreException e) {
            return Response.serverError().entity("Core error: " + e.getMessage()).build();
        } catch (Exception e) {
            return Response.serverError().entity("Other error: " + e.getMessage()).build();
        }
    }
}
