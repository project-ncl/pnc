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

import com.wordnik.swagger.annotations.*;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(value = "/running-build-records", description = "Build Records for running builds")
@Path("/running-build-records")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RunningBuildRecordEndpoint extends AbstractEndpoint<BuildRecord, BuildRecordRest> {

    private BuildRecordProvider buildRecordProvider;

    public RunningBuildRecordEndpoint() {
    }

    @Inject
    public RunningBuildRecordEndpoint(BuildRecordProvider buildRecordProvider) {
        super(buildRecordProvider);
        this.buildRecordProvider = buildRecordProvider;
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval all RunningBuildRecords"),
            @ApiResponse(code = 204, message = "No RunningBuildRecords available"),
    })
    @ApiOperation(value = "Gets all running Build Records", responseContainer = "List", response = BuildRecordRest.class)
    @GET
    public Response getAll(@ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") Integer pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") Integer pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        return fromCollection(buildRecordProvider.getAllRunning(pageIndex, pageSize, sortingRsql, rsql));
    }

    @ApiOperation(value = "Gets specific running Build Record", response = BuildRecordRest.class)
    @GET
    @Path("/{id}")
    public Response getSpecific(@ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id) {
        return fromSingleton(buildRecordProvider.getSpecificRunning(id));
    }
}
