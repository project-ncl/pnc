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
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.provider.UserProvider;
import org.jboss.pnc.rest.restmodel.UserRest;
import org.jboss.pnc.rest.validation.exceptions.ValidationException;

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
import javax.ws.rs.core.UriInfo;

@Api(value = "/users", description = "User related information")
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserEndpoint extends AbstractEndpoint<User, UserRest> {

    public UserEndpoint() {
    }

    @Inject
    public UserEndpoint(UserProvider userProvider) {
        super(userProvider);
    }

    @ApiOperation(value = "Gets all Users", responseContainer = "List", response = UserRest.class)
    @GET
    public Response getAll(@ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query") @QueryParam("q") String rsql) {
        return super.getAll(pageIndex, pageSize, sortingRsql, rsql);
    }

    @ApiOperation(value = "Gets specific User", response = UserRest.class)
    @GET
    @Path("/{id}")
    public Response getSpecific(@ApiParam(value = "User id", required = true) @PathParam("id") Integer id) {
        return super.getSpecific(id);
    }

    @ApiOperation(value = "Creates new User", response = UserRest.class)
    @POST
    public Response createNew(@NotNull @Valid UserRest userRest, @Context UriInfo uriInfo) throws ValidationException {
        return super.createNew(userRest, uriInfo);
    }

    @ApiOperation(value = "Updates an existing User")
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "User id", required = true) @PathParam("id") Integer id,
            @NotNull @Valid UserRest userRest) throws ValidationException {
       return super.update(id, userRest);
    }

}
