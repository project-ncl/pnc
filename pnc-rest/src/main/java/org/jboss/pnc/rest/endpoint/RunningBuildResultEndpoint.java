package org.jboss.pnc.rest.endpoint;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.jboss.pnc.rest.provider.BuildResultProvider;
import org.jboss.pnc.rest.restmodel.BuildResultRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.invoke.MethodHandles;
import java.util.List;

@Api(value = "/result/running", description = "Results for running builds")
@Path("/result/running")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RunningBuildResultEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private BuildResultProvider buildResultProvider;

    public RunningBuildResultEndpoint() {
    }

    @Inject
    public RunningBuildResultEndpoint(BuildResultProvider buildCollectionProvider) {
        this.buildResultProvider = buildCollectionProvider;
    }

    @ApiOperation(value = "Gets all running Build Results")
    @GET
    public List<BuildResultRest> getAll() {
        return buildResultProvider.getAllRunning();
    }

    @ApiOperation(value = "Gets specific Build Collection")
    @GET
    @Path("/{id}")
    public BuildResultRest getSpecific(
            @ApiParam(value = "BuildResult id", required = true) @PathParam("id") String id) {
        return buildResultProvider.getSpecificRunning(id);
    }

    @ApiOperation(value = "Gets specific Build Collection")
    @GET
    @Path("/{id}/log")
    public Response getLogs(
            @ApiParam(value = "BuildResult id", required = true) @PathParam("id") String id) {
        return Response.ok(buildResultProvider.getLogsForRunningBuildId(id)).build();
    }
}
