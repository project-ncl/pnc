package org.jboss.pnc.rest.endpoint;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Api(value = "/result", description = "Results of building process")
@Path("/result")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildRecordEndpoint {

    private BuildRecordProvider buildRecordProvider;

    public BuildRecordEndpoint() {
    }

    @Inject
    public BuildRecordEndpoint(BuildRecordProvider buildRecordProvider) {
        this.buildRecordProvider = buildRecordProvider;
    }

    @ApiOperation(value = "Gets all Build Records")
    @GET
    public List<BuildRecordRest> getAll() {
        return buildRecordProvider.getAllArchived();
    }

    @ApiOperation(value = "Gets specific Build Record")
    @GET
    @Path("/{id}")
    public BuildRecordRest getSpecific(@ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id) {
        return buildRecordProvider.getSpecific(id);
    }

    @ApiOperation(value = "Gets logs for specific Build Record")
    @GET
    @Path("/{id}/log")
    public Response getLogs(@ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id) {
        return Response.ok(buildRecordProvider.getLogsForBuildId(id)).build();
    }
}
