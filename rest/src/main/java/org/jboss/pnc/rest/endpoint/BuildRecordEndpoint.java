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

import org.jboss.pnc.rest.provider.ArtifactProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.ArtifactRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationAuditedRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.utils.Utility;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.util.List;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Api(value = "/build-records", description = "Records of build executions")
@Path("/build-records")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildRecordEndpoint {

    private BuildRecordProvider buildRecordProvider;

    private ArtifactProvider artifactProvider;

    public BuildRecordEndpoint() {
    }

    @Inject
    public BuildRecordEndpoint(BuildRecordProvider buildRecordProvider, ArtifactProvider artifactProvider) {
        this.buildRecordProvider = buildRecordProvider;
        this.artifactProvider = artifactProvider;
    }

    @ApiOperation(value = "Gets all Build Records")
    @GET
    public List<BuildRecordRest> getAll(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        return buildRecordProvider.getAllArchived(pageIndex, pageSize, sortingRsql, rsql);
    }

    @ApiOperation(value = "Gets specific Build Record")
    @GET
    @Path("/{id}")
    public Response getSpecific(@ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id) {
        return Utility.createRestEnityResponse(buildRecordProvider.getSpecific(id), id);
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of the log"),
            @ApiResponse(code = 204, message = "BuildRecord exists, but the content is empty"),
            @ApiResponse(code = 404, message = "BuildRecord with specified id does not exist"),
    })
    @ApiOperation(value = "Gets logs for specific Build Record")
    @GET
    @Path("/{id}/log")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getLogs(@ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id) {
        String buildRecordLog = buildRecordProvider.getBuildRecordLog(id);
        if (buildRecordLog == null)
            return Response.status(Status.NOT_FOUND).build();

        if (buildRecordLog.isEmpty())
            return Response.noContent().build();
        else
            return Response.ok(buildRecordProvider.getLogsForBuild(buildRecordLog)).build();
    }

    @ApiOperation(value = "Gets artifacts for specific Build Record")
    @GET
    @Path("/{id}/artifacts")
    public List<ArtifactRest> getArtifacts(
            @ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        return artifactProvider.getAll(pageIndex, pageSize, sortingRsql, rsql, id);
    }

    @ApiOperation(value = "Gets the Build Records linked to a specific Build Configuration")
    @GET
    @Path("/build-configurations/{configurationId}")
    public List<BuildRecordRest> getAllForBuildConfiguration(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql,
            @ApiParam(value = "Build Configuration id", required = true) @PathParam("configurationId") Integer configurationId) {
        return buildRecordProvider.getAllForBuildConfiguration(pageIndex, pageSize, sortingRsql, rsql, configurationId);
    }

    @ApiOperation(value = "Gets the Build Records linked to a specific Project")
    @GET
    @Path("/projects/{projectId}")
    public List<BuildRecordRest> getAllForProject(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "Project id", required = true) @PathParam("projectId") Integer projectId,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        return buildRecordProvider.getAllForProject(pageIndex, pageSize, sortingRsql, rsql, projectId);
    }

    @ApiOperation(value = "Gets the audited build configuration for specific build record")
    @GET
    @Path("/{id}/build-configuration-audited")
    public Response getBuildConfigurationAudited(@ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id) {
        return buildRecordProvider.getBuildConfigurationAudited(id);
    }

}
