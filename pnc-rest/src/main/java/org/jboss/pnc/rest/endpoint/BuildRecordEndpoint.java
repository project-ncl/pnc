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
    public Response getAll(@ApiParam(value = "Page index", required = false) @QueryParam("pageIndex") Integer pageIndex,
            @ApiParam(value = "Pagination size", required = false) @QueryParam("pageSize") Integer pageSize,
            @ApiParam(value = "Sorting field", required = false) @QueryParam("sorted_by") String field,
            @ApiParam(value = "Sort direction", required = false) @QueryParam("sorting") String sorting,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        return Response.ok(buildRecordProvider.getAllArchived(pageIndex, pageSize, field, sorting, rsql)).build();
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

    /**
     * TODO avibelli - (need to wait for additional fields in BuildRecord)
     * 
     * GET /record/configuration/{configurationId} Gets the Build Records triggered by a specific Build Configuration
     */
}
