package org.jboss.pnc.rest.endpoint;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import org.jboss.pnc.rest.provider.UserProvider;
import org.jboss.pnc.rest.restmodel.UserRest;
import org.jboss.pnc.rest.validation.WithNullId;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.util.List;

@Api(value = "/user", description = "User related information")
@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserEndpoint {

    private UserProvider userProvider;

    public UserEndpoint() {
    }

    @Inject
    public UserEndpoint(UserProvider userProvider) {
        this.userProvider = userProvider;
    }

    @ApiOperation(value = "Gets all Users")
    @GET
    public List<UserRest> getAll(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query") @QueryParam("q") String rsql) {
        return userProvider.getAll(pageIndex, pageSize, sortingRsql, rsql);
    }

    @ApiOperation(value = "Gets specific User")
    @GET
    @Path("/{id}")
    public UserRest getSpecific(@ApiParam(value = "User id", required = true) @PathParam("id") Integer id) {
        return userProvider.getSpecific(id);
    }

    @ApiOperation(value = "Creates new User")
    @POST
    public Response createNew(@NotNull @Valid @WithNullId UserRest userRest, @Context UriInfo uriInfo) {
        int id = userProvider.store(userRest);
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getRequestUri()).path("{id}");
        return Response.created(uriBuilder.build(id)).entity(userProvider.getSpecific(id)).build();
    }

}
