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
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.provider.BuildConfigurationSetProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.provider.ConflictedEntryException;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.trigger.BuildTriggerer;
import org.jboss.pnc.spi.datastore.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.lang.invoke.MethodHandles;
import java.net.URL;

@Api(value = "/build-configuration-sets", description = "Set of related build configurations")
@Path("/build-configuration-sets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildConfigurationSetEndpoint extends AbstractEndpoint<BuildConfigurationSet, BuildConfigurationSetRest> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private BuildTriggerer buildTriggerer;
    
    @Context
    private HttpServletRequest httpServletRequest;
    
    @Inject
    private Datastore datastore;

    private BuildConfigurationSetProvider buildConfigurationSetProvider;
    private BuildConfigurationProvider buildConfigurationProvider;
    private BuildRecordProvider buildRecordProvider;

    public BuildConfigurationSetEndpoint() {

    }

    @Inject
    public BuildConfigurationSetEndpoint(BuildConfigurationSetProvider buildConfigurationSetProvider,
            BuildTriggerer buildTriggerer, BuildConfigurationProvider buildConfigurationProvider,
            BuildRecordProvider buildRecordProvider) {
        super(buildConfigurationSetProvider);
        this.buildConfigurationSetProvider = buildConfigurationSetProvider;
        this.buildTriggerer = buildTriggerer;
        this.buildConfigurationProvider = buildConfigurationProvider;
        this.buildRecordProvider = buildRecordProvider;
    }

    @ApiOperation(value = "Gets all Build Configuration Sets",
            responseContainer = "List", response = BuildConfigurationSetRest.class)
    @GET
    public Response getAll(@ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        return super.getAll(pageIndex, pageSize, sortingRsql, rsql);
    }

    @ApiOperation(value = "Creates a new Build Configuration Set", response = BuildConfigurationSetRest.class)
    @POST
    public Response createNew(@NotNull @Valid BuildConfigurationSetRest buildConfigurationSetRest, @Context UriInfo uriInfo)
            throws ConflictedEntryException {
        return super.createNew(buildConfigurationSetRest, uriInfo);
    }

    @ApiOperation(value = "Gets a specific Build Configuration Set", response = BuildConfigurationSetRest.class)
    @GET
    @Path("/{id}")
    public Response getSpecific(
            @ApiParam(value = "Build Configuration Set id", required = true) @PathParam("id") Integer id) {
        return super.getSpecific(id);
    }

    @ApiOperation(value = "Updates an existing Build Configuration Set")
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "Build Configuration Set id", required = true) @PathParam("id") Integer id,
            @NotNull @Valid BuildConfigurationSetRest buildConfigurationSetRest)
            throws ConflictedEntryException {
        return super.update(id, buildConfigurationSetRest);
    }

    @ApiOperation(value = "Removes a specific Build Configuration Set")
    @DELETE
    @Path("/{id}")
    public Response deleteSpecific(@ApiParam(value = "Build Configuration Set id", required = true) @PathParam("id") Integer id) {
        return super.delete(id);
    }

    @ApiOperation(value = "Gets the Configurations for the Specified Set",
            responseContainer = "List", response = BuildConfigurationRest.class)
    @GET
    @Path("/{id}/build-configurations")
    public Response getConfigurations(@ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql,
            @ApiParam(value = "Build Configuration Set id", required = true) @PathParam("id") Integer id) {
        return fromCollection(buildConfigurationProvider.getAllForBuildConfigurationSet(pageIndex, pageSize, sortingRsql, rsql,
                id));
    }

    @ApiOperation(value = "Adds a configuration to the Specified Set")
    @POST
    @Path("/{id}/build-configurations")
    public Response addConfiguration(
            @ApiParam(value = "Build Configuration Set id", required = true) @PathParam("id") Integer id,
            BuildConfigurationRest buildConfig) throws ConflictedEntryException {
        buildConfigurationSetProvider.addConfiguration(id, buildConfig.getId());
        return fromEmpty();
    }

    @ApiOperation(value = "Removes a configuration from the specified config set")
    @DELETE
    @Path("/{id}/build-configurations/{configId}")
    public Response removeConfiguration(
            @ApiParam(value = "Build configuration set id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = "Build configuration id", required = true) @PathParam("configId") Integer configId) {
        buildConfigurationSetProvider.removeConfiguration(id, configId);
        return fromEmpty();
    }

    @ApiOperation(value = "Gets all build records associated with the contained build configurations",
            responseContainer = "List", response = BuildRecordRest.class)
    @GET
    @Path("/{id}/build-records")
    public Response getBuildRecords(
            @ApiParam(value = "Build configuration set id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        return fromCollection(buildRecordProvider.getAllForBuildConfigSetRecord(pageIndex, pageSize, sortingRsql, rsql, id));
    }

    @ApiOperation(value = "Builds the Configurations for the Specified Set")
    @POST
    @Path("/{id}/build")
    @Consumes(MediaType.WILDCARD)
    public Response build(
            @ApiParam(value = "Build Configuration Set id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = "Optional Callback URL", required = false) @QueryParam("callbackUrl") String callbackUrl,
            @Context UriInfo uriInfo) {
        logger.info("Executing build configuration set id: " + id );

        try {
            AuthenticationProvider authProvider = new AuthenticationProvider(httpServletRequest);
            String loggedUser = authProvider.getUserName();
            User currentUser = null;
            if(loggedUser != null && loggedUser != "") {
                currentUser = datastore.retrieveUserByUsername(loggedUser);
            }
            if(currentUser == null) {
                currentUser = User.Builder.newBuilder()
                        .username(loggedUser)
                        .firstName(authProvider.getFirstName())
                        .lastName(authProvider.getLastName())
                        .email(authProvider.getEmail()).build();
                datastore.createNewUser(currentUser);
            }
            Integer runningBuildId = null;
            // if callbackUrl is provided trigger build accordingly
            if (callbackUrl == null || callbackUrl.isEmpty()) {
                runningBuildId = buildTriggerer.triggerBuildConfigurationSet(id, currentUser);
            } else {
                runningBuildId = buildTriggerer.triggerBuildConfigurationSet(id, currentUser, new URL(callbackUrl));
            }

            return Response.ok().build();
        } catch (CoreException e) {
            logger.error(e.getMessage(), e);
            return Response.serverError().entity("Core error: " + e.getMessage()).build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Response.serverError().entity("Other error: " + e.getMessage()).build();
        }
    }

}
