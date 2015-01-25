package org.jboss.pnc.rest.endpoint;

import com.wordnik.swagger.annotations.*;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.rest.trigger.BuildTriggerer;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.lang.invoke.MethodHandles;
import java.net.URI;

@Api(value = "/product/{productId}/version/{versionId}/project/{projectId}/configuration", description = "Triggering build configuration")
@Path("/product/{productId}/version/{versionId}/project/{projectId}/configuration")
public class TriggerBuildEndpoint {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    private BuildTriggerer buildTriggerer;

    public TriggerBuildEndpoint() {
    }

    @Inject
    public TriggerBuildEndpoint(BuildTriggerer buildTriggerer) {
        this.buildTriggerer = buildTriggerer;
    }

    @ApiOperation(value = "Triggers build")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Everything is OK"),
            @ApiResponse(code = 500, message = "Something went wrong")
    })
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/build")
    public Response getSpecificBuild(
            @ApiParam(value = "Product id", required = true) @PathParam("productId") Integer productId,
            @ApiParam(value = "Product Version id", required = true) @PathParam("versionId") Integer productVersionId,
            @ApiParam(value = "Project id", required = true) @PathParam("projectId") Integer projectId,
            @ApiParam(value = "Configuration id", required = true) @PathParam("id") Integer id,
            @Context UriInfo uriInfo) {
        try {
            Integer runningBuildId = buildTriggerer.triggerBuilds(id);
            UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path("/result/running/{id}");
            URI uri = uriBuilder.build(runningBuildId);
            return Response.ok(uri).entity(uri).build();
        } catch (CoreException e) {
            logger.error(e.getMessage(), e);
            return Response.serverError().entity("Core error: " + e.getMessage()).build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Response.serverError().entity("Other error: " + e.getMessage()).build();
        }
    }
}
