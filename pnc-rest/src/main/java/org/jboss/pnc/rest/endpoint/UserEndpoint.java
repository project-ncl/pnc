package org.jboss.pnc.rest.endpoint;

import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.jboss.pnc.rest.provider.UserProvider;
import org.jboss.pnc.rest.restmodel.UserRest;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

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
    public List<UserRest> getAll() {
        return userProvider.getAll();
    }

    @ApiOperation(value = "Gets specific User")
    @GET
    @Path("/{id}")
    public UserRest getSpecific(@ApiParam(value = "User id", required = true) @PathParam("id") Integer id) {
        return userProvider.getSpecific(id);
    }

    @ApiOperation(value = "Creates new User")
    @POST
    public Response createNew(@NotNull @Valid UserRest userRest, @Context UriInfo uriInfo) {
        int id = userProvider.store(userRest);
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getRequestUri()).path("{id}");
        return Response.created(uriBuilder.build(id)).build();
    }

}
