package org.jboss.pnc.rest.endpoint;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import org.jboss.pnc.rest.provider.LicenseProvider;
import org.jboss.pnc.rest.restmodel.LicenseRest;
import org.jboss.pnc.rest.validation.WithNullId;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.util.List;

/**
 * Created by avibelli on Feb 5, 2015
 *
 */
@Api(value = "/license", description = "License related information")
@Path("/license")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LicenseEndpoint {

    private LicenseProvider licenseProvider;

    public LicenseEndpoint() {
    }

    @Inject
    public LicenseEndpoint(LicenseProvider licenseProvider) {
        this.licenseProvider = licenseProvider;
    }

    @ApiOperation(value = "Gets all Licenses")
    @GET
    public List<LicenseRest> getAll(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        return licenseProvider.getAll(pageIndex, pageSize, sortingRsql, rsql);
    }

    @ApiOperation(value = "Get specific License")
    @GET
    @Path("/{id}")
    public LicenseRest getSpecific(@ApiParam(value = "License id", required = true) @PathParam("id") Integer id) {
        return licenseProvider.getSpecific(id);
    }

    @ApiOperation(value = "Creates a new License")
    @POST
    public Response createNew(@NotNull @Valid @WithNullId LicenseRest licenseRest, @Context UriInfo uriInfo) {
        int id = licenseProvider.store(licenseRest);
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getRequestUri()).path("{id}");
        return Response.created(uriBuilder.build(id)).entity(licenseProvider.getSpecific(id)).build();
    }

    @ApiOperation(value = "Updates an existing License")
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "License id", required = true) @PathParam("id") Integer licenseId,
            @NotNull @Valid @WithNullId LicenseRest licenseRest, @Context UriInfo uriInfo) {
        licenseRest.setId(licenseId);
        licenseProvider.update(licenseRest);
        return Response.ok().build();
    }

    @ApiOperation(value = "Deletes an existing License")
    @DELETE
    @Path("/{id}")
    public Response delete(@ApiParam(value = "License id", required = true) @PathParam("id") Integer licenseId) {
        licenseProvider.delete(licenseId);
        return Response.ok().build();
    }
}
