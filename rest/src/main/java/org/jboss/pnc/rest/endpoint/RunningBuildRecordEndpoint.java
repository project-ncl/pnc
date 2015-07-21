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
import org.jboss.logging.Logger;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.utils.Utility;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.invoke.MethodHandles;
import java.util.List;

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
    public List<BuildRecordRest> getAll(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") Integer pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") Integer pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        return buildRecordProvider.getAllRunning(pageIndex, pageSize, sortingRsql, rsql);
    }

    @ApiOperation(value = "Gets specific running Build Record")
    @GET
    @Path("/{id}")
    public Response getSpecific(@ApiParam(value = "BuildRecord id", required = true) @PathParam("id") Integer id) {
        return Utility.createRestEnityResponse(buildRecordProvider.getSpecificRunning(id), id);
    }
}
