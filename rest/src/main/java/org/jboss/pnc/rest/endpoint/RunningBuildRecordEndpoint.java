/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.rest.provider.BuildConfigSetRecordProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SEARCH_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SEARCH_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SORTING_QUERY_PARAM;

@Hidden
@Path("/running-build-records")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RunningBuildRecordEndpoint extends AbstractEndpoint<BuildRecord, BuildRecordRest> {

    private BuildRecordProvider buildRecordProvider;
    private BuildCoordinator buildCoordinator;
    private static final Logger logger = LoggerFactory.getLogger(RunningBuildRecordEndpoint.class);
    private BuildConfigSetRecordProvider buildConfigSetRecordProvider;

    public RunningBuildRecordEndpoint() {
    }

    @Inject
    public RunningBuildRecordEndpoint(BuildRecordProvider buildRecordProvider,
                                      BuildCoordinator buildCoordinator,
                                      BuildConfigSetRecordProvider buildConfigSetRecordProvider) {
        super(buildRecordProvider);
        this.buildConfigSetRecordProvider = buildConfigSetRecordProvider;
        this.buildRecordProvider = buildRecordProvider;
        this.buildCoordinator = buildCoordinator;
    }

    @GET
    public Response getAll(@QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(SEARCH_QUERY_PARAM) @DefaultValue(SEARCH_DEFAULT_VALUE) String search) {
        return fromCollection(buildRecordProvider.getAllRunning(pageIndex, pageSize, search, sort));
    }

    @GET
    @Path("/count")
    public Response getCount() {
        return Response.ok(buildRecordProvider.getRunningCount()).build();

    }

    @GET
    @Path("/{id}")
    public Response getSpecific(@PathParam("id") Integer id) {
        return fromSingleton(buildRecordProvider.getSpecificRunning(id));
    }

    @GET
    @Path("/build-configurations/{id}")
    public Response getAllForBC(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SEARCH_QUERY_PARAM) @DefaultValue(SEARCH_DEFAULT_VALUE) String search,
            @PathParam("id") Integer bcId) {
        return fromCollection(buildRecordProvider.getAllRunningForBuildConfiguration(pageIndex, pageSize, search, "", bcId));
    }

    @GET
    @Path("/build-config-set-records/{id}")
    public Response getAllForBCSetRecord(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SEARCH_QUERY_PARAM) @DefaultValue(SEARCH_DEFAULT_VALUE) String search,
            @PathParam("id") Integer bcSetRecordId) {
        return fromCollection(buildRecordProvider.getAllRunningForBCSetRecord(pageIndex, pageSize, search, bcSetRecordId));
    }

    @POST
    @Path("/build-config-set-records/{id}/cancel")
    public Response cancelAllBuildsInGroup(
            @PathParam("id") Integer bcSetRecordId) {
        logger.debug("Received cancel request fot Build Configuration Set: {}.", bcSetRecordId);
        if (buildConfigSetRecordProvider.getSpecific(bcSetRecordId) == null) {
            logger.error("Unable to find Build Configuration Set: {}.", bcSetRecordId);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        try {
            buildCoordinator.cancelSet(bcSetRecordId.intValue());
        } catch (CoreException e) {
            logger.error("Error when canceling buildConfigSetRecord with id: {}", bcSetRecordId, e);
            return Response.serverError().build();
        }
        return Response.ok().build();
    }
}
