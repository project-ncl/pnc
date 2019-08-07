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
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.User;
import org.jboss.pnc.pncmetrics.rest.TimedMetric;
import org.jboss.pnc.rest.provider.ArtifactProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.restmodel.graph.GraphRest;
import org.jboss.pnc.rest.utils.EndpointAuthenticationProvider;
import org.jboss.pnc.rest.validation.exceptions.RepositoryViolationException;
import org.jboss.pnc.spi.exception.ValidationException;
import org.jboss.pnc.spi.notifications.Notifier;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.function.Consumer;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.QUERY_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SORTING_QUERY_PARAM;

@Hidden
@Path("/build-records")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildRecordEndpoint extends AbstractEndpoint<BuildRecord, BuildRecordRest> {

    private BuildRecordProvider buildRecordProvider;
    private ArtifactProvider artifactProvider;
    private EndpointAuthenticationProvider authProvider;

    private TemporaryBuildsCleanerAsyncInvoker temporaryBuildsCleanerAsyncInvoker;

    private Notifier notifier;

    @Context
    private HttpServletRequest httpServletRequest;

    @Deprecated //CDI workaround
    public BuildRecordEndpoint() {
    }

    @Inject
    public BuildRecordEndpoint(
            BuildRecordProvider buildRecordProvider,
            ArtifactProvider artifactProvider,
            EndpointAuthenticationProvider authProvider,
            TemporaryBuildsCleanerAsyncInvoker temporaryBuildsCleanerAsyncInvoker,
            Notifier notifier) {
        super(buildRecordProvider);
        this.buildRecordProvider = buildRecordProvider;
        this.artifactProvider = artifactProvider;
        this.authProvider = authProvider;
        this.temporaryBuildsCleanerAsyncInvoker = temporaryBuildsCleanerAsyncInvoker;
        this.notifier = notifier;
    }

    @GET
    @Override
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
    public Response getSpecific(@PathParam("id") Integer id) {
        return super.getSpecific(id);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id)
            throws RepositoryViolationException {

        User currentUser = authProvider.getCurrentUser(httpServletRequest);
        boolean found;
        try {
            found = temporaryBuildsCleanerAsyncInvoker.deleteTemporaryBuild(id, currentUser.getLoginToken(), (t) -> {});
        } catch (ValidationException e) {
            throw new RepositoryViolationException(e);
        }

        if (found) {
            return Response.ok().build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/{id}/log")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getLogs(@PathParam("id") Integer id) {
        String buildRecordLog = buildRecordProvider.getBuildRecordLog(id);
        if (buildRecordLog == null)
            return Response.status(Status.NOT_FOUND).build();

        if (buildRecordLog.isEmpty())
            return Response.noContent().build();
        else
            return Response.ok(buildRecordProvider.getLogsForBuild(buildRecordLog)).build();
    }

    @GET
    @Path("/{id}/repour-log")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getRepourLogs(@PathParam("id") Integer id) {
        String log = buildRecordProvider.getBuildRecordRepourLog(id);
        if (log == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        if (log.isEmpty()) {
            return Response.noContent().build();
        } else {
            return Response.ok(buildRecordProvider.getRepourLogsForBuild(log)).build();
        }
    }

    @GET
    @Path("/{id}/built-artifacts")
    public Response getBuiltArtifacts(@PathParam("id") Integer id,
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q) {
        this.getSpecific(id);
        return fromCollection(artifactProvider.getBuiltArtifactsForBuildRecord(pageIndex, pageSize, sort, q, id));
    }

    @GET
    @Path("/{id}/dependency-artifacts")
    public Response getDependencyArtifacts(@PathParam("id") Integer id,
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q) {
        return fromCollection(artifactProvider.getDependencyArtifactsForBuildRecord(pageIndex, pageSize, sort, q, id));
    }

    /**
     * @deprecated
     * Use /build-configuration/{id}/build-records
     */
    @Deprecated
    @GET
    @Path("/build-configurations/{configurationId}")
    public Response getAllForBuildConfiguration(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q,
            @PathParam("configurationId") Integer configurationId) {
        return fromCollection(
                buildRecordProvider.getAllForBuildConfiguration(pageIndex, pageSize, sort, q, configurationId));
    }

    @GET
    @Path("/projects/{projectId}")
    public Response getAllForProject(@QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @QueryParam("sort") String sortingRsql,
            @PathParam("projectId") Integer projectId,
            @QueryParam("q") String rsql) {
        return fromCollection(buildRecordProvider.getAllForProject(pageIndex, pageSize, sortingRsql, rsql, projectId));
    }

    @GET
    @Path("/build-configuration-or-project-name/{name}")
    public Response getAllForProject(@QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @QueryParam("sort") String sortingRsql,
            @PathParam("name") String name,
            @QueryParam("q") String rsql) {
        return fromCollection(buildRecordProvider.getAllForConfigurationOrProjectName(pageIndex, pageSize, sortingRsql, rsql, name));
    }

    @GET
    @Path("/{id}/build-configuration-audited")
    public Response getBuildConfigurationAudited(@PathParam("id") Integer id) {
        return fromSingleton(buildRecordProvider.getBuildConfigurationAudited(id));
    }

    @POST
    @Path("/{id}/put-attribute")
    public Response putAttribute(@PathParam("id") Integer id,
                              @QueryParam("key") String key,
                              @QueryParam("value") String value) {
        buildRecordProvider.putAttribute(id, key, value);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}/remove-attribute")
    public Response removeAttribute(@PathParam("id") Integer id,
                              @QueryParam("key") String key) {
        buildRecordProvider.removeAttribute(id, key);
        return Response.ok().build();
    }

    @GET
    @Path("/{id}/get-attributes")
    public Response getAttributes(@PathParam("id") Integer id) {
        return fromSingleton(buildRecordProvider.getAttributes(id));
    }

    @GET
    @Path("/get-by-attribute")
    public Response queryByAttribute(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q,
            @QueryParam("key") String key,
            @QueryParam("value") String value) {
        return fromCollection(buildRecordProvider.getByAttribute(pageIndex,pageSize,sort,q, key, value));
    }

    /**
     * @deprecated
     * Use /builds/{id}
     *
     * Gets a BuildRecord which is completed or in running state
     */
    @GET
    @Path("/{id}/completed-or-running")
    public Response getCompletedOrRunnning(@PathParam("id") Integer id) {

        Response resp = getSpecific(id);
        if (resp.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
          resp = fromSingleton(buildRecordProvider.getSpecificRunning(id));
        }
        return resp;
    }

    @GET
    @Path("/{id}/dependency-graph")
    public Response getDependencyGraph(@PathParam("id") Integer bcId) {
        GraphRest<BuildRecordRest> dependencyGraph = buildRecordProvider.getDependencyGraphRest(bcId);
        return fromSingleton(dependencyGraph);
    }

}
