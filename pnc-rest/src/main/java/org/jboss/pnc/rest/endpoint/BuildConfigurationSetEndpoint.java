package org.jboss.pnc.rest.endpoint;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.jboss.pnc.rest.provider.BuildConfigurationSetProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.trigger.BuildTriggerer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Api(value = "/config-set", description = "Set of related build configurations")
@Path("/config-set")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildConfigurationSetEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private BuildConfigurationSetProvider buildConfigurationSetProvider;
    private BuildTriggerer buildTriggerer;

    public BuildConfigurationSetEndpoint() {
    }

    @Inject
    public BuildConfigurationSetEndpoint(BuildConfigurationSetProvider buildConfigurationSetProvider, BuildTriggerer buildTriggerer) {
        this.buildConfigurationSetProvider = buildConfigurationSetProvider;
        this.buildTriggerer = buildTriggerer;
    }

    @ApiOperation(value = "Gets all Build Configuration Sets")
    @GET
    public Response getAll(@ApiParam(value = "Page index", required = false) @QueryParam("pageIndex") Integer pageIndex,
            @ApiParam(value = "Pagination size", required = false) @QueryParam("pageSize") Integer pageSize,
            @ApiParam(value = "Sorting field", required = false) @QueryParam("sorted_by") String field,
            @ApiParam(value = "Sort direction", required = false) @QueryParam("sorting") String sorting,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {

        return Response.ok(buildConfigurationSetProvider.getAll(pageIndex, pageSize, field, sorting, rsql)).build();
    }

    @ApiOperation(value = "Creates a new Build Configuration Set")
    @POST
    public Response createNew(@NotNull @Valid BuildConfigurationSetRest buildConfigurationSetRest, @Context UriInfo uriInfo) {
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getRequestUri()).path("{id}");
        int id = buildConfigurationSetProvider.store(buildConfigurationSetRest);
        return Response.created(uriBuilder.build(id)).entity(buildConfigurationSetProvider.getSpecific(id)).build();
    }

    @ApiOperation(value = "Gets a specific Build Configuration Set")
    @GET
    @Path("/{id}")
    public BuildConfigurationSetRest getSpecific(
            @ApiParam(value = "Build Configuration Set id", required = true) @PathParam("id") Integer id) {
        return buildConfigurationSetProvider.getSpecific(id);
    }

}
