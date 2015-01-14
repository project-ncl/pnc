package org.jboss.pnc.rest.endpoint;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
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
public class RunningBuildRecordEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private BuildRecordProvider buildRecordProvider;

    public RunningBuildRecordEndpoint() {
    }

    @Inject
    public RunningBuildRecordEndpoint(BuildRecordProvider buildRecordProvider) {
        this.buildRecordProvider = buildRecordProvider;
    }

    @ApiOperation(value = "Gets all running Build Records")
    @GET
    public List<BuildRecordRest> getAll() {
        return buildRecordProvider.getAllRunning();
    }

    @ApiOperation(value = "Gets specific Build Collection")
    @GET
    @Path("/{id}")
    public BuildRecordRest getSpecific(
            @ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id) {
        return buildRecordProvider.getSpecificRunning(id);
    }

    @ApiOperation(value = "Gets specific Build Collection")
    @GET
    @Path("/{id}/log")
    public Response getLogs(
            @ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id) {
        return Response.ok(buildRecordProvider.getLogsForRunningBuildId(id)).build();
    }
}
