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

import org.jboss.pnc.rest.provider.BuildConfigSetRecordProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigSetRecordRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.utils.Utility;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.List;
import java.util.Set;

@Api(value = "/build-config-set-records", description = "Records of the build config set executions")
@Path("/build-config-set-records")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildConfigSetRecordEndpoint {

    private BuildConfigSetRecordProvider buildConfigSetRecordProvider;
    private BuildRecordProvider buildRecordProvider;

    public BuildConfigSetRecordEndpoint() {
    }

    @Inject
    public BuildConfigSetRecordEndpoint(BuildConfigSetRecordProvider buildConfigSetRecordProvider, BuildRecordProvider buildRecordProvider) {
        this.buildConfigSetRecordProvider = buildConfigSetRecordProvider;
        this.buildRecordProvider = buildRecordProvider;
    }

    @ApiOperation(value = "Gets all build config set execution records", responseContainer = "List",
            response = BuildConfigSetRecordRest.class)
    @GET
    public List<BuildConfigSetRecordRest> getAll(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        return buildConfigSetRecordProvider.getAll(pageIndex, pageSize, sortingRsql, rsql);
    }

    @ApiOperation(value = "Gets specific build config set execution record", response = BuildConfigSetRecordRest.class)
    @GET
    @Path("/{id}")
    public Response getSpecific(@ApiParam(value = "BuildConfigSetRecord id", required = true) @PathParam("id") Integer id) {
        return Utility.createRestEnityResponse(buildConfigSetRecordProvider.getSpecific(id), id);
    }

    @ApiOperation(value = "Gets the build records associated with this set",
            responseContainer = "List", response = BuildRecordRest.class)
    @GET
    @Path("/{id}/build-records")
    public List<BuildRecordRest> getBuildRecords(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql,
            @ApiParam(value = "Build Config set record id", required = true) @PathParam("id") Integer id) {
        return buildConfigSetRecordProvider.getBuildRecords(pageIndex, pageSize, sortingRsql, rsql, id);
    }

}
