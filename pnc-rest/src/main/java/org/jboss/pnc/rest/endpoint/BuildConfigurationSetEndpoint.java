package org.jboss.pnc.rest.endpoint;

import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.jboss.pnc.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.rest.provider.BuildConfigurationSetProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.trigger.BuildTriggerer;
import org.jboss.pnc.rest.validation.WithNullId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Api(value = "/configuration-set", description = "Set of related build configurations")
@Path("/configuration-set")
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
    public List<BuildConfigurationSetRest> getAll(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {

        return buildConfigurationSetProvider.getAll(pageIndex, pageSize, sortingRsql, rsql);
    }

    @ApiOperation(value = "Creates a new Build Configuration Set")
    @POST
    public Response createNew(@NotNull @Valid @WithNullId BuildConfigurationSetRest buildConfigurationSetRest, @Context UriInfo uriInfo) {
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

    @ApiOperation(value = "Updates an existing Build Configuration Set")
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "Build Configuration Set id", required = true) @PathParam("id") Integer id,
            @NotNull @Valid @WithNullId BuildConfigurationSetRest buildConfigurationSetRest, @Context UriInfo uriInfo) {
        buildConfigurationSetRest.setId(id);
        buildConfigurationSetProvider.update(buildConfigurationSetRest);
        return Response.ok().build();
    }

    @ApiOperation(value = "Removes a specific Build Configuration Set")
    @DELETE
    @Path("/{id}")
    public Response deleteSpecific(@ApiParam(value = "Build Configuration Set id", required = true) @PathParam("id") Integer id) {
        buildConfigurationSetProvider.delete(id);
        return Response.ok().build();
    }

    @ApiOperation(value = "Gets the Configurations for the Specified Set")
    @GET
    @Path("/{id}/configurations")
    public List<BuildConfigurationRest> getConfigurations(
            @ApiParam(value = "Build Configuration Set id", required = true) @PathParam("id") Integer id) {
        return buildConfigurationSetProvider.getBuildConfigurations(id);
    }

    @ApiOperation(value = "Builds the Configurations for the Specified Set")
    @POST
    @Path("/{id}/build")
    public Response build(
            @ApiParam(value = "Build Configuration Set id", required = true) @PathParam("id") Integer id) {
        BuildConfigurationSetRest buildConfigSet = buildConfigurationSetProvider.getSpecific(id);
        logger.info("Executing build configuration set: " + buildConfigSet.getName() );
        // This is just a place holder until the logic is added for executing a set
        logger.info("Not currently implemented");
        return Response.ok().build();
    }

    @ApiOperation(value = "Adds a configuration to the Specified Set")
    @POST
    @Path("/{id}/configurations")
    public Response addConfiguration(
            @ApiParam(value = "Build Configuration Set id", required = true) @PathParam("id") Integer id,
            BuildConfigurationRest buildConfig) {
        buildConfigurationSetProvider.addConfiguration(id, buildConfig.getId());
        return Response.ok().build();
    }

    @ApiOperation(value = "Removes a configuration from the specified config set")
    @DELETE
    @Path("/{id}/configurations/{configId}")
    public Response addConfiguration(
            @ApiParam(value = "Build configuration set id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = "Build configuration id", required = true) @PathParam("configId") Integer configId) {
        buildConfigurationSetProvider.removeConfiguration(id, configId);
        return Response.ok().build();
    }

}
