package org.jboss.pnc.rest.endpoint;

import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.jboss.pnc.rest.provider.ProductVersionProvider;
import org.jboss.pnc.rest.provider.ProjectProvider;
import org.jboss.pnc.rest.restmodel.ProductVersionRest;
import org.jboss.pnc.rest.restmodel.ProjectRest;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Api(value = "/product/{productId}/version", description = "Product Version related information")
@Path("/product/{productId}/version")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductVersionEndpoint {

    private ProductVersionProvider productVersionProvider;

    public ProductVersionEndpoint() {
    }

    @Inject
    public ProductVersionEndpoint(ProductVersionProvider productVersionProvider, ProjectProvider projectProvider) {
        this.productVersionProvider = productVersionProvider;
    }

    @ApiOperation(value = "Gets all Product Versions")
    @GET
    public List<ProductVersionRest> getAll(
            @ApiParam(value = "Product id", required = true) @PathParam("productId") Integer productId) {
        return productVersionProvider.getAll(productId);
    }

    @ApiOperation(value = "Gets specific Product Version")
    @GET
    @Path("/{id}")
    public ProductVersionRest getSpecific(
            @ApiParam(value = "Product id", required = true) @PathParam("productId") Integer productId,
            @ApiParam(value = "Product Version id", required = true) @PathParam("id") Integer id) {
        return productVersionProvider.getSpecific(productId, id);
    }

    @ApiOperation(value = "Creates a new Product Version")
    @POST
    public Response createNew(@ApiParam(value = "Product id", required = true) @PathParam("productId") Integer productId,
            @NotNull @Valid ProductVersionRest productVersionRest, @Context UriInfo uriInfo) {
        int id = productVersionProvider.store(productId, productVersionRest);
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getRequestUri()).path("{id}");
        return Response.created(uriBuilder.build(id)).build();
    }

    @ApiOperation(value = "Updates an existing Product Version")
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "Product id", required = true) @PathParam("productId") Integer productId,
            @ApiParam(value = "Product Version id", required = true) @PathParam("id") Integer id,
            @NotNull @Valid ProductVersionRest productVersionRest, @Context UriInfo uriInfo) {
        productVersionProvider.update(productVersionRest);
        return Response.ok().build();
    }

}
