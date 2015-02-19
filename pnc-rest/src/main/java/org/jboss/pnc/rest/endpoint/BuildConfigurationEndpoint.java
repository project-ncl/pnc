package org.jboss.pnc.rest.endpoint;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.trigger.BuildTriggerer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;

@Api(value = "/configuration", description = "Build Configuration related information")
@Path("/configuration")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildConfigurationEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private BuildConfigurationProvider buildConfigurationProvider;
    private BuildTriggerer buildTriggerer;
    private BuildRecordProvider buildRecordProvider;

    public BuildConfigurationEndpoint() {
    }

    @Inject
    public BuildConfigurationEndpoint(BuildConfigurationProvider buildConfigurationProvider, BuildTriggerer buildTriggerer, BuildRecordProvider buildRecordProvider) {
        this.buildConfigurationProvider = buildConfigurationProvider;
        this.buildTriggerer = buildTriggerer;
        this.buildRecordProvider = buildRecordProvider;
    }

    @ApiOperation(value = "Gets all Build Configurations")
    @GET
    public Response getAll(@ApiParam(value = "Page index", required = false) @QueryParam("pageIndex") Integer pageIndex,
            @ApiParam(value = "Pagination size", required = false) @QueryParam("pageSize") Integer pageSize,
            @ApiParam(value = "Sorting field", required = false) @QueryParam("sorted_by") String field,
            @ApiParam(value = "Sort direction", required = false) @QueryParam("sorting") String sorting,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {

        return Response.ok(buildConfigurationProvider.getAll(pageIndex, pageSize, field, sorting, rsql)).build();
    }

    @ApiOperation(value = "Creates a new Build Configuration")
    @POST
    public Response createNew(@NotNull @Valid BuildConfigurationRest buildConfigurationRest, @Context UriInfo uriInfo) {
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getRequestUri()).path("{id}");
        int id = buildConfigurationProvider.store(buildConfigurationRest);
        return Response.created(uriBuilder.build(id)).entity(buildConfigurationProvider.getSpecific(id)).build();
    }

    @ApiOperation(value = "Gets a specific Build Configuration")
    @GET
    @Path("/{id}")
    public BuildConfigurationRest getSpecific(
            @ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id) {
        return buildConfigurationProvider.getSpecific(id);
    }

    @ApiOperation(value = "Updates an existing Build Configuration")
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id,
            @NotNull @Valid BuildConfigurationRest buildConfigurationRest, @Context UriInfo uriInfo) {
        buildConfigurationProvider.update(buildConfigurationRest);
        return Response.ok().build();
    }

    @ApiOperation(value = "Removes a specific Build Configuration")
    @DELETE
    @Path("/{id}")
    public Response deleteSpecific(@ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id) {
        buildConfigurationProvider.delete(id);
        return Response.ok().build();
    }

    @ApiOperation(value = "Clones an existing Build Configuration")
    @POST
    @Path("/{id}/clone")
    public Response clone(@ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id,
            @Context UriInfo uriInfo) {
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path("/configuration/{id}");
        int newId = buildConfigurationProvider.clone(id);
        return Response.created(uriBuilder.build(newId)).entity(buildConfigurationProvider.getSpecific(newId)).build();
    }

    @ApiOperation(value = "Triggers the build of a specific Build Configuration")
    @POST
    @Path("/{id}/build")
    @Consumes(MediaType.WILDCARD)
    public Response trigger(@ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id,
            @Context UriInfo uriInfo) {
        try {
            Integer runningBuildId = buildTriggerer.triggerBuilds(id);
            UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path("/result/running/{id}");
            URI uri = uriBuilder.build(runningBuildId);
            return Response.ok(uri).header("location", uri).entity(buildRecordProvider.getSpecificRunning(runningBuildId)).build();
        } catch (CoreException e) {
            logger.error(e.getMessage(), e);
            return Response.serverError().entity("Core error: " + e.getMessage()).build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Response.serverError().entity("Other error: " + e.getMessage()).build();
        }
    }

    @ApiOperation(value = "Gets all Build Configurations of a Project")
    @GET
    @Path("/project/{projectId}")
    public List<BuildConfigurationRest> getAllByProjectId(
            @ApiParam(value = "Project id", required = true) @PathParam("projectId") Integer projectId) {
        return buildConfigurationProvider.getAllForProject(projectId);
    }

    @ApiOperation(value = "Gets all Build Configurations of a Product")
    @GET
    @Path("/product/{productId}")
    public List<BuildConfigurationRest> getAllByProductId(
            @ApiParam(value = "Product id", required = true) @PathParam("productId") Integer productId) {
        return buildConfigurationProvider.getAllForProduct(productId);
    }

    @ApiOperation(value = "Gets all Build Configurations of a Product Version")
    @GET
    @Path("/product/{productId}/version/{versionId}")
    public List<BuildConfigurationRest> getAllByProductId(
            @ApiParam(value = "Product id", required = true) @PathParam("productId") Integer productId,
            @ApiParam(value = "Product Version id id", required = true) @PathParam("versionId") Integer versionId) {
        return buildConfigurationProvider.getAllForProductAndProductVersion(productId, versionId);
    }
}
