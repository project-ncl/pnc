package org.jboss.pnc.rest.endpoint;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.jboss.pnc.rest.provider.ProjectProvider;
import org.jboss.pnc.rest.restmodel.ProjectRest;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Api(value = "/product/{productId}/version/{versionId}/project", description = "Project related information")
@Path("/product/{productId}/version/{versionId}/project")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProjectEndpoint {

    private ProjectProvider projectProvider;

    public ProjectEndpoint() {
    }

    @Inject
    public ProjectEndpoint(ProjectProvider projectProvider) {
        this.projectProvider = projectProvider;
    }

    @ApiOperation(value = "Gets all Projects")
    @GET
    public List<ProjectRest> getAll(
            @ApiParam(value = "Product id", required = true) @PathParam("productId") Integer productId,
            @ApiParam(value = "Product Version id", required = true) @PathParam("versionId") Integer productVersionId) {
        return projectProvider.getAll(productId, productVersionId);
    }

    @ApiOperation(value = "Gets specific Project")
    @GET
    @Path("/{id}")
    public ProjectRest getSpecific(
            @ApiParam(value = "Product id", required = true) @PathParam("productId") Integer productId,
            @ApiParam(value = "Product Version id", required = true) @PathParam("versionId") Integer productVersionId,
            @ApiParam(value = "Project id", required = true) @PathParam("id") Integer id) {
        return projectProvider.getSpecific(productId, productVersionId, id);
    }
}
