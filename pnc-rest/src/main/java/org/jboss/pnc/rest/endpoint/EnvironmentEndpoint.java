package org.jboss.pnc.rest.endpoint;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.jboss.pnc.rest.provider.EnvironmentProvider;
import org.jboss.pnc.rest.restmodel.EnvironmentRest;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Api(value = "/environment", description = "Environment related information")
@Path("/environment")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EnvironmentEndpoint {

    private EnvironmentProvider environmentProvider;

    public EnvironmentEndpoint() {
    }

    @Inject
    public EnvironmentEndpoint(EnvironmentProvider environmentProvider) {
        this.environmentProvider = environmentProvider;
    }

    @ApiOperation(value = "Gets all Environments")
    @GET
    public Response getAll(@ApiParam(value = "Page index", required = false) @QueryParam("pageIndex") Integer pageIndex,
            @ApiParam(value = "Pagination size", required = false) @QueryParam("pageSize") Integer pageSize,
            @ApiParam(value = "Sorting field", required = false) @QueryParam("sorted_by") String field,
            @ApiParam(value = "Sort direction", required = false) @QueryParam("sorting") String sorting,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        return Response.ok(environmentProvider.getAll(pageIndex, pageSize, field, sorting, rsql)).build();
    }

    @ApiOperation(value = "Get specific Environment")
    @GET
    @Path("/{id}")
    public EnvironmentRest getSpecific(@ApiParam(value = "Environment id", required = true) @PathParam("id") Integer id) {
        return environmentProvider.getSpecific(id);
    }

    @ApiOperation(value = "Creates a new Environment")
    @POST
    public Response createNew(@NotNull @Valid EnvironmentRest environmentRest, @Context UriInfo uriInfo) {
        int id = environmentProvider.store(environmentRest);
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getRequestUri()).path("{id}");
        return Response.created(uriBuilder.build(id)).entity(environmentProvider.getSpecific(id)).build();
    }

    @ApiOperation(value = "Updates an existing Environment")
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "Environment id", required = true) @PathParam("id") Integer environmentId,
            @NotNull @Valid EnvironmentRest environmentRest, @Context UriInfo uriInfo) {
        environmentProvider.update(environmentRest);
        return Response.ok().build();
    }

    @ApiOperation(value = "Deletes an existing Product")
    @DELETE
    @Path("/{id}")
    public Response delete(@ApiParam(value = "License id", required = true) @PathParam("id") Integer licenseId) {
        environmentProvider.delete(licenseId);
        return Response.ok().build();
    }
}
