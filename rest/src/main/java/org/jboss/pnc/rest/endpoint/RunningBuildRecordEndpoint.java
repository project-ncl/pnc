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

import org.jboss.logging.Logger;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;

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

import java.lang.invoke.MethodHandles;
import java.util.List;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Api(value = "/running-build-records", description = "Build Records for running builds")
@Path("/running-build-records")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RunningBuildRecordEndpoint {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    private BuildRecordProvider buildRecordProvider;

    public RunningBuildRecordEndpoint() {
    }

    @Inject
    public RunningBuildRecordEndpoint(BuildRecordProvider buildRecordProvider) {
        this.buildRecordProvider = buildRecordProvider;
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval all RunningBuildRecords"),
            @ApiResponse(code = 204, message = "No RunningBuildRecords available"),
    })
    @ApiOperation(value = "Gets all running Build Records")
    @GET
    public Response getAll(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") Integer pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") Integer pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        List<BuildRecordRest> allRecords = buildRecordProvider.getAllRunning(pageIndex, pageSize, sortingRsql, rsql);
        if (allRecords.isEmpty())
            return Response.status(Status.NO_CONTENT).build();
        else
            return Response.ok(allRecords).build();
    }

    @ApiOperation(value = "Gets specific running Build Record")
    @GET
    @Path("/{id}")
    public BuildRecordRest getSpecific(@ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id) {
        return buildRecordProvider.getSpecificRunning(id);
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of the log"),
            @ApiResponse(code = 204, message = "RunningBuild exists, but the log content is empty"),
            @ApiResponse(code = 404, message = "RunningBuild with specified id does not exist"),
    })
    @ApiOperation(value = "Gets specific log of a Running Build Record")
    @GET
    @Path("/{id}/log")
    public Response getLogs(@ApiParam(value = "RunningBuild id", required = true) @PathParam("id") Integer id) {
        String buildLog = buildRecordProvider.getSubmittedBuildLog(id);
        if (buildLog == null)
            return Response.status(Status.NOT_FOUND).build();

        if (buildLog.isEmpty())
            return Response.noContent().build();
        else
            return Response.ok(buildRecordProvider.getStreamingOutputForString(buildLog)).build();
    }
}
