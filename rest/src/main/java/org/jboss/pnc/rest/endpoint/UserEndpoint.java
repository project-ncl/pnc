/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.rest.endpoint;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import org.jboss.pnc.rest.provider.UserProvider;
import org.jboss.pnc.rest.restmodel.UserRest;
import org.jboss.pnc.rest.utils.Utility;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.util.List;

@Api(value = "/users", description = "User related information")
@Path("/users")
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

    @ApiOperation(value = "Gets all Users", responseContainer = "List", response = UserRest.class)
    @GET
    public List<UserRest> getAll(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query") @QueryParam("q") String rsql) {
        return userProvider.getAll(pageIndex, pageSize, sortingRsql, rsql);
    }

    @ApiOperation(value = "Gets specific User", response = UserRest.class)
    @GET
    @Path("/{id}")
    public Response getSpecific(@ApiParam(value = "User id", required = true) @PathParam("id") Integer id) {
        return Utility.createRestEnityResponse(userProvider.getSpecific(id), id);
    }

    @ApiOperation(value = "Creates new User", response = UserRest.class)
    @POST
    public Response createNew(@NotNull @Valid UserRest userRest, @Context UriInfo uriInfo) {
        int id = userProvider.store(userRest);
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getRequestUri()).path("{id}");
        return Response.created(uriBuilder.build(id)).entity(userProvider.getSpecific(id)).build();
    }

    @ApiOperation(value = "Updates an existing User")
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "User id", required = true) @PathParam("id") Integer id,
            @NotNull @Valid UserRest userRest, @Context UriInfo uriInfo) {
        userProvider.update(id, userRest);
        return Response.ok().build();
    }

}
