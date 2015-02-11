package org.jboss.pnc.rest.endpoint;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
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

    public BuildConfigurationEndpoint() {
    }

    @Inject
    public BuildConfigurationEndpoint(BuildConfigurationProvider buildConfigurationProvider, BuildTriggerer buildTriggerer) {
        this.buildConfigurationProvider = buildConfigurationProvider;
        this.buildTriggerer = buildTriggerer;
    }

    @ApiOperation(value = "Gets all Build Configurations")
    @GET
    public List<BuildConfigurationRest> getAll() {
        return buildConfigurationProvider.getAll();
    }

    @ApiOperation(value = "Creates new Project's Build Configuration")
    @POST
    public Response createNew(@NotNull @Valid BuildConfigurationRest buildConfigurationRest, @Context UriInfo uriInfo) {
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getRequestUri()).path("{id}");
        int id = buildConfigurationProvider.store(buildConfigurationRest);
        return Response.created(uriBuilder.build(id)).build();
    }

    @ApiOperation(value = "Gets a specific Project's Build Configuration")
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

    @ApiOperation(value = "Removes a specific Project's Build Configuration")
    @DELETE
    @Path("/{id}")
    public Response deleteSpecific(
            @ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id) {
        buildConfigurationProvider.delete(id);
        return Response.ok().build();
    }

    @ApiOperation(value = "Clone a Build Configuration")
    @POST
    @Path("/{id}/clone")
    public Response clone(@ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id, @Context UriInfo uriInfo) {
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path("/configuration/{id}");
        int newId = buildConfigurationProvider.clone(id);
        return Response.created(uriBuilder.build(newId)).build();
    }

    @ApiOperation(value = "Triggers a specific Build Configuration to build")
    @POST
    @Path("/{id}/build")
    @Consumes(MediaType.WILDCARD)
    public Response trigger(@ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id, @Context UriInfo uriInfo) {
        try {
            Integer runningBuildId = buildTriggerer.triggerBuilds(id);
            UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path("/result/running/{id}");
            URI uri = uriBuilder.build(runningBuildId);
            return Response.ok(uri).entity(uri).build();
        } catch (CoreException e) {
            logger.error(e.getMessage(), e);
            return Response.serverError().entity("Core error: " + e.getMessage()).build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Response.serverError().entity("Other error: " + e.getMessage()).build();
        }
    }

    @ApiOperation(value = "Gets all Build Configurations")
    @GET
    @Path("/configuration/project/{projectId}")
    public List<BuildConfigurationRest> getAllByProjectId(@ApiParam(value = "Project id", required = true) @PathParam("projectId") Integer projectId) {
        return buildConfigurationProvider.getAllForProject(projectId);
    }

    @ApiOperation(value = "Gets all Build Configurations")
    @GET
    @Path("/configuration/product/{productId}")
    public List<BuildConfigurationRest> getAllByProductId(@ApiParam(value = "Product id", required = true) @PathParam("productId") Integer productId) {
        return buildConfigurationProvider.getAllForProduct(productId);
    }

    @ApiOperation(value = "Gets all Build Configurations")
    @GET
    @Path("/configuration/product/{productId}/version/{versionId}")
    public List<BuildConfigurationRest> getAllByProductId(@ApiParam(value = "Product id", required = true) @PathParam("productId") Integer productId,
                                                          @ApiParam(value = "Product Version id id", required = true) @PathParam("versionId") Integer versionId) {
        return buildConfigurationProvider.getAllForProductAndProductVersion(productId, versionId);
    }
}
