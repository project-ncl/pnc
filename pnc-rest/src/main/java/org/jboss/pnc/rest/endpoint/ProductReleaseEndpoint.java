package org.jboss.pnc.rest.endpoint;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.jboss.pnc.rest.provider.ProductReleaseProvider;
import org.jboss.pnc.rest.provider.ProjectProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.restmodel.ProductReleaseRest;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

@Api(value = "/product-release", description = "Product Release related information")
@Path("/product-release")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductReleaseEndpoint {

    private ProductReleaseProvider productReleaseProvider;

    public ProductReleaseEndpoint() {
    }

    @Inject
    public ProductReleaseEndpoint(ProductReleaseProvider productReleaseProvider, ProjectProvider projectProvider) {
        this.productReleaseProvider = productReleaseProvider;
    }

    @ApiOperation(value = "Gets all Product Releases")
    @GET
    public List<ProductReleaseRest> getAll(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query") @QueryParam("q") String rsql) {
        return productReleaseProvider.getAll(pageIndex, pageSize, sortingRsql, rsql);
    }

    @ApiOperation(value = "Gets specific Product Release")
    @GET
    @Path("/{id}")
    public ProductReleaseRest getSpecific(
            @ApiParam(value = "Product Release id", required = true) @PathParam("id") Integer id) {
        return productReleaseProvider.getSpecific(id);
    }

    @ApiOperation(value = "Creates a new Product Release")
    @POST
    public Response createNew(@NotNull @Valid ProductReleaseRest productReleaseRest, @Context UriInfo uriInfo) {
        int id = productReleaseProvider.store(productReleaseRest);
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getRequestUri()).path("{id}");
        return Response.created(uriBuilder.build(id)).entity(productReleaseProvider.getSpecific(id)).build();
    }

    @ApiOperation(value = "Updates an existing Product Release")
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "Product Release id", required = true) @PathParam("id") Integer id,
            @NotNull @Valid ProductReleaseRest productReleaseRest, @Context UriInfo uriInfo) {
        productReleaseProvider.update(id, productReleaseRest);
        return Response.ok().build();
    }

}
