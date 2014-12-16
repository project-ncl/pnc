package org.jboss.pnc.rest.endpoint;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.jboss.pnc.rest.provider.BuildResultProvider;
import org.jboss.pnc.rest.restmodel.BuildResultRest;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api(value = "/result", description = "Results of building process")
@Path("/result")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildResultEndpoint {

    private BuildResultProvider buildResultProvider;

    public BuildResultEndpoint() {
    }

    @Inject
    public BuildResultEndpoint(BuildResultProvider buildCollectionProvider) {
        this.buildResultProvider = buildCollectionProvider;
    }

    @ApiOperation(value = "Gets all Build Results")
    @GET
    public List<BuildResultRest> getAll() {
        return buildResultProvider.getAllArchived();
    }

    @ApiOperation(value = "Gets specific Build Collection")
    @GET
    @Path("/{id}")
    public BuildResultRest getSpecific(
            @ApiParam(value = "BuildResult id", required = true) @PathParam("id") Integer id) {
        return buildResultProvider.getSpecific(id);
    }

    @ApiOperation(value = "Gets logs for specific Build Result")
    @GET
    @Path("/{id}/log")
    public Response getLogs(
            @ApiParam(value = "BuildResult id", required = true) @PathParam("id") Integer id) {
        return Response.ok(buildResultProvider.getLogsForBuildId(id)).build();
    }
}
