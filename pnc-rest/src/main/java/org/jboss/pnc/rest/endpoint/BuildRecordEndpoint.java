package org.jboss.pnc.rest.endpoint;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api(value = "/record", description = "Records of building process")
@Path("/record")
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
    public List<BuildRecordRest> getAll(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") Integer pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") Integer pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        return buildRecordProvider.getAllArchived(pageIndex, pageSize, sortingRsql, rsql);
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

    @ApiOperation(value = "Gets the Build Records linked to a specific Build Configuration")
    @GET
    @Path("/configuration/{configurationId}")
    public List<BuildRecordRest> getAllForBuildConfiguration(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") Integer pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") Integer pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql,
            @ApiParam(value = "Build Configuration id", required = true) @PathParam("configurationId") Integer configurationId) {
        return buildRecordProvider.getAllForBuildConfiguration(pageIndex, pageSize, sortingRsql, rsql, configurationId);
    }

    @ApiOperation(value = "Gets the Build Records linked to a specific Project")
    @GET
    @Path("/project/{projectId}")
    public List<BuildRecordRest> getAllForProject(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") Integer pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") Integer pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "Project id", required = true) @PathParam("projectId") Integer projectId,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        return buildRecordProvider.getAllForProject(pageIndex, pageSize, sortingRsql, rsql, projectId);
    }
}
