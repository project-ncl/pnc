package org.jboss.pnc.rest.endpoint;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.jboss.pnc.rest.restmodel.ProductRest;
import org.jboss.pnc.rest.provider.ProductProvider;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

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

    @ApiOperation(value = "Gets all products")
    @GET
    public List<ProductRest> getAll() {
        return productProvider.getAll();
    }

    @ApiOperation(value = "Gets specific Product")
    @GET
    @Path("/{id}")
    public ProductRest getSpecific(
            @ApiParam(value = "Product id", required = true) @PathParam("id") Integer id) {
        return productProvider.getSpecific(id);
    }
}
