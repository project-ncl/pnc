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
import org.jboss.pnc.coordinator.maintenance.Result;
import org.jboss.pnc.coordinator.maintenance.TemporaryBuildsCleanerAsyncInvoker;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.User;
import org.jboss.pnc.pncmetrics.rest.TimedMetric;
import org.jboss.pnc.rest.provider.BuildConfigSetRecordProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigSetRecordRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.restmodel.graph.GraphRest;
import org.jboss.pnc.rest.utils.EndpointAuthenticationProvider;
import org.jboss.pnc.rest.validation.exceptions.RepositoryViolationException;
import org.jboss.pnc.spi.exception.ValidationException;
import org.jboss.pnc.spi.notifications.Notifier;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.function.Consumer;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.QUERY_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SORTING_QUERY_PARAM;

@Hidden
@Path("/build-config-set-records")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildConfigSetRecordEndpoint extends AbstractEndpoint<BuildConfigSetRecord, BuildConfigSetRecordRest> {

    private BuildRecordProvider buildRecordProvider;

    private TemporaryBuildsCleanerAsyncInvoker temporaryBuildsCleanerAsyncInvoker;

    private EndpointAuthenticationProvider authenticationProvider;

    @Context
    private HttpServletRequest httpServletRequest;

    private Notifier notifier;

    @Deprecated //CDI workaround
    public BuildConfigSetRecordEndpoint() {
    }

    @Inject
    public BuildConfigSetRecordEndpoint(
            BuildConfigSetRecordProvider buildConfigSetRecordProvider,
            BuildRecordProvider buildRecordProvider,
            TemporaryBuildsCleanerAsyncInvoker temporaryBuildsCleanerAsyncInvoker,
            EndpointAuthenticationProvider authenticationProvider, Notifier notifier) {
        super(buildConfigSetRecordProvider);
        this.buildRecordProvider = buildRecordProvider;
        this.temporaryBuildsCleanerAsyncInvoker = temporaryBuildsCleanerAsyncInvoker;
        this.authenticationProvider = authenticationProvider;
        this.notifier = notifier;
    }

    @GET
    @TimedMetric
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

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id)
            throws RepositoryViolationException {
        User currentUser = authenticationProvider.getCurrentUser(httpServletRequest);

        try {
            temporaryBuildsCleanerAsyncInvoker.deleteTemporaryBuildConfigSetRecord(id, currentUser.getLoginToken(), (t) -> {});
        } catch (ValidationException e) {
            throw new RepositoryViolationException(e);
        }
        return Response.ok().build();
    }

    @GET
    @Path("/{id}/build-records")
    public Response getBuildRecords(@QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q,
            @PathParam("id") Integer id) {
        return fromCollection(buildRecordProvider.getAllForBuildConfigSetRecord(pageIndex, pageSize, sort, q, id));
    }

    @GET
    @Path("/{id}/dependency-graph")
    public Response getDependencyGraphForSet(@PathParam("id") Integer bcSetRecordId) {
        GraphRest<BuildRecordRest> dependencyGraph = buildRecordProvider.getBCSetRecordRestGraph(bcSetRecordId);
        return fromSingleton(dependencyGraph);
    }
}
