package org.jboss.pnc.rest.endpoint;

import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
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

import org.jboss.pnc.rest.provider.ProductMilestoneProvider;
import org.jboss.pnc.rest.provider.ProjectProvider;
import org.jboss.pnc.rest.restmodel.ProductMilestoneRest;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Api(value = "/product-milestones", description = "Product Milestone related information")
@Path("/product-milestones")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductMilestoneEndpoint {

    private ProductMilestoneProvider productMilestoneProvider;

    public ProductMilestoneEndpoint() {
    }

    @Inject
    public ProductMilestoneEndpoint(ProductMilestoneProvider productMilestoneProvider, ProjectProvider projectProvider) {
        this.productMilestoneProvider = productMilestoneProvider;
    }

    @ApiOperation(value = "Gets all Product Milestones")
    @GET
    public List<ProductMilestoneRest> getAll(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query") @QueryParam("q") String rsql) {
        return productMilestoneProvider.getAll(pageIndex, pageSize, sortingRsql, rsql);
    }

    @ApiOperation(value = "Gets all Product Milestones of the Specified Product Version")
    @GET
    @Path("/product-versions/{versionId}")
    public List<ProductMilestoneRest> getAllByProductVersionId(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql,
            @ApiParam(value = "Product Version id", required = true) @PathParam("versionId") Integer versionId) {

        return productMilestoneProvider.getAllForProductVersion(pageIndex, pageSize, sortingRsql, rsql, versionId);
    }

    @ApiOperation(value = "Gets specific Product Milestone")
    @GET
    @Path("/{id}")
    public ProductMilestoneRest getSpecific(
            @ApiParam(value = "Product Milestone id", required = true) @PathParam("id") Integer id) {
        return productMilestoneProvider.getSpecific(id);
    }

    @ApiOperation(value = "Creates a new Product Milestone for the Specified Product Version")
    @POST
    @Path("/product-versions/{versionId}")
    public Response createNew(
            @ApiParam(value = "Product Version id", required = true) @PathParam("versionId") Integer versionId,
            @NotNull @Valid ProductMilestoneRest productMilestoneRest, @Context UriInfo uriInfo) {

        int id = productMilestoneProvider.store(versionId, productMilestoneRest);
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getRequestUri()).path("{id}");
        return Response.created(uriBuilder.build(id)).entity(productMilestoneProvider.getSpecific(id)).build();
    }

    @ApiOperation(value = "Updates an existing Product Milestone")
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "Product Milestone id", required = true) @PathParam("id") Integer id,
            @NotNull @Valid ProductMilestoneRest productMilestoneRest, @Context UriInfo uriInfo) {
        productMilestoneProvider.update(id, productMilestoneRest);
        return Response.ok().build();
    }

}
