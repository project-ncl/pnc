package org.jboss.pnc.rest.endpoint;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.jboss.pnc.rest.provider.ProductProvider;
import org.jboss.pnc.rest.restmodel.ProductRest;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(value = "/product", description = "Product related information")
@Path("/product")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductEndpoint {

    private ProductProvider productProvider;

    public ProductEndpoint() {
    }

    @Inject
    public ProductEndpoint(ProductProvider productProvider) {
        this.productProvider = productProvider;
    }

    @ApiOperation(value = "Gets all Products")
    @GET
    public Response getAll(@ApiParam(value = "Page index", required = false) @QueryParam("pageIndex") Integer pageIndex,
            @ApiParam(value = "Pagination size", required = false) @QueryParam("pageSize") Integer pageSize,
            @ApiParam(value = "Sorting field", required = false) @QueryParam("sorted_by") String field,
            @ApiParam(value = "Sort direction", required = false) @QueryParam("sorting") String sorting,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        return Response.ok(productProvider.getAll(pageIndex, pageSize, field, sorting, rsql)).build();
    }

    @ApiOperation(value = "Get specific Product")
    @GET
    @Path("/{id}")
    public ProductRest getSpecific(@ApiParam(value = "Product id", required = true) @PathParam("id") Integer id) {
        return productProvider.getSpecific(id);
    }

}
