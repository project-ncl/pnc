package org.jboss.pnc.rest.endpoint;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import org.jboss.pnc.rest.restmodel.ProductVersionRest;
import org.jboss.pnc.rest.restmodel.ProjectRest;
import org.jboss.pnc.rest.provider.ProductVersionProvider;
import org.jboss.pnc.rest.provider.ProjectProvider;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.List;

@Api(value = "/product/{productId}/version", description = "Product Version related information")
@Path("/product/{productId}/version")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductVersionEndpoint {

    private ProductVersionProvider productProviderProvider;
    private ProjectProvider projectProvider;

    public ProductVersionEndpoint() {
    }

    @Inject
    public ProductVersionEndpoint(ProductVersionProvider productProviderProvider, ProjectProvider projectProvider) {
        this.productProviderProvider = productProviderProvider;
        this.projectProvider = projectProvider;
    }

    @ApiOperation(value = "Gets all Product Versions")
    @GET
    public List<ProductVersionRest> getAll(
            @ApiParam(value = "Product id", required = true) @PathParam("productId") Integer productId) {
        return productProviderProvider.getAll(productId);
    }

    @ApiOperation(value = "Gets specific Product Version")
    @GET
    @Path("/{id}")
    public ProductVersionRest getSpecific(
            @ApiParam(value = "Product id", required = true) @PathParam("productId") Integer productId,
            @ApiParam(value = "Product Version id", required = true) @PathParam("id") Integer id) {
        return productProviderProvider.getSpecific(productId, id);
    }

    @ApiOperation(value = "Gets all Projects of a specific Product Version")
    @GET
    @Path("/{id}/project")
    public List<ProjectRest> getAllProjectsOfProductVersion(
            @ApiParam(value = "Product id", required = true) @PathParam("productId") Integer productId,
            @ApiParam(value = "Product Version id", required = true) @PathParam("id") Integer productVersionId) {
        return projectProvider.getAll(productId, productVersionId);
    }
}
