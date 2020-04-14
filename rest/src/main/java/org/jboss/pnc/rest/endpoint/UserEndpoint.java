/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import io.swagger.v3.oas.annotations.Hidden;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.AuthenticationProviderFactory;
import org.jboss.pnc.auth.LoggedInUser;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.provider.UserProvider;
import org.jboss.pnc.rest.restmodel.UserRest;
import org.jboss.pnc.rest.utils.ErrorResponse;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.spi.datastore.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
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
import java.lang.invoke.MethodHandles;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.QUERY_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SORTING_QUERY_PARAM;

@Hidden
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserEndpoint extends AbstractEndpoint<User, UserRest> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private BuildRecordProvider buildRecordProvider;

    private AuthenticationProvider authenticationProvider;

    private Datastore datastore;

    public UserEndpoint() {
    }

    @Inject
    public UserEndpoint(
            UserProvider userProvider,
            BuildRecordProvider buildRecordProvider,
            Datastore datastore,
            AuthenticationProviderFactory authenticationProviderFactory) {
        super(userProvider);
        this.buildRecordProvider = buildRecordProvider;
        this.datastore = datastore;
        this.authenticationProvider = authenticationProviderFactory.getProvider();
    }

    @GET
    public Response getAll(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q) {
        return super.getAll(pageIndex, pageSize, sort, q);
    }

    @GET
    @Path("/{id}")
    public Response getSpecific(@PathParam("id") @NotNull Integer id) {
        return super.getSpecific(id);
    }

    @POST
    public Response createNew(UserRest userRest, @Context UriInfo uriInfo) throws RestValidationException {
        return super.createNew(userRest, uriInfo);
    }

    @POST
    @Path("/loggedUser")
    public Response getLoggedUser(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest)
            throws RestValidationException {
        try {
            LoggedInUser loginInUser = authenticationProvider.getLoggedInUser(httpServletRequest);

            String loggedUser = loginInUser.getUserName().intern();

            synchronized (loggedUser) {
                User currentUser = datastore.retrieveUserByUsername(loggedUser);
                if (currentUser != null) {
                    return super.getSpecific(currentUser.getId());
                }

                currentUser = User.Builder.newBuilder()
                        .username(loggedUser)
                        .firstName(loginInUser.getFirstName())
                        .lastName(loginInUser.getLastName())
                        .email(loginInUser.getEmail())
                        .build();
                return super.createNew(new UserRest(currentUser), uriInfo);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ErrorResponse.toResponse(e);
        }

    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, UserRest userRest) throws RestValidationException {
        return super.update(id, userRest);
    }

    @GET
    @Path("/{id}/builds")
    public Response getBuilds(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q,
            @PathParam("id") Integer id) {
        return fromCollection(
                buildRecordProvider.getRunningAndCompletedBuildRecordsByUserId(pageIndex, pageSize, sort, q, id));
    }

}
