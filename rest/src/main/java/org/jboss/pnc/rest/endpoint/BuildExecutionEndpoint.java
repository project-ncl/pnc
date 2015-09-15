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
import com.wordnik.swagger.annotations.ApiParam;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.core.builder.executor.BuildExecutionTask;
import org.jboss.pnc.core.builder.executor.BuildExecutor;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.utils.BpmCallback;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Api(value = "/builder", description = "Build tasks.")
@Path("/builder")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildExecutionEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(BuildExecutionEndpoint.class);
    private BuildExecutor buildExecutor;

    @Context
    private HttpServletRequest httpServletRequest;

    private Datastore datastore;
    private BuildConfigurationRepository buildConfigurationRepository;
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;
    private BpmCallback bpmCallback;
    private BuildRecordProvider buildRecordProvider;

    @Inject
    public BuildExecutionEndpoint(BuildExecutor buildExecutor, Datastore datastore, BuildConfigurationRepository buildConfigurationRepository, BpmCallback bpmCallback, BuildRecordProvider buildRecordProvider) {
        this.buildExecutor = buildExecutor;
        this.datastore = datastore;
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.bpmCallback = bpmCallback;
        this.buildRecordProvider = buildRecordProvider;
    }

    @Deprecated
    public BuildExecutionEndpoint() {} // CDI workaround

    @POST
    @Path("/{taskId}/build")
    @Consumes(MediaType.WILDCARD)
    public Response build(@ApiParam(value = "Build Configuration id", required = true) @PathParam("configuration-id") Integer buildConfigurationId,
                          @ApiParam(value = "Build Configuration revision", required = true) @PathParam("configuration-revision") Integer buildConfigurationRevision,
                          @ApiParam(value = "Build task id", required = false) @QueryParam("build-set-task-id") int buildTaskId,
                          @ApiParam(value = "Build set task id", required = false) @QueryParam("build-set-task-id") int buildSetTaskId,
                          @ApiParam(value = "Optional Callback URL", required = false) @QueryParam("callback-url") String callbackUrl,
                          @ApiParam(value = "A CSV list of build record set ids.", required = false) @QueryParam("build-record-set-ids") String buildRecordSetIdsCSV,
                          @ApiParam(value = "Build configuration set record id.", required = false) @QueryParam("callback-url") String buildConfigSetRecordId,
                          @Context UriInfo uriInfo) {
        try {
            AuthenticationProvider authProvider = new AuthenticationProvider(httpServletRequest);
            String loggedUser = authProvider.getUserName();
            User currentUser = null;
            if(loggedUser != null && loggedUser != "") {
                currentUser = datastore.retrieveUserByUsername(loggedUser);
            } else {
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            final BuildConfiguration configuration = buildConfigurationRepository.queryById(buildConfigurationId);
            IdRev idRev = new IdRev(buildConfigurationId, buildConfigurationRevision);
            final BuildConfigurationAudited configurationAudited = buildConfigurationAuditedRepository.queryById(idRev);

            Consumer<BuildStatus> onComplete = (buildStatus) -> {
                if (callbackUrl == null || callbackUrl.isEmpty()) {
                    // Expecting URL like: http://host:port/business-central/rest/runtime/org.test:Test1:1.0/process/instance/7/signal?signal=testSig
                    bpmCallback.signalBpmEvent(callbackUrl.toString() + "&event=" + buildStatus);
                }
            };

            Set<Integer> buildRecordSetIds = parseIntegers(buildRecordSetIdsCSV);
            BuildExecutionTask buildExecutionTask = buildExecutor.build(
                    configuration,
                    configurationAudited,
                    currentUser,
                    onComplete,
                    buildRecordSetIds,
                    Integer.parseInt(buildConfigSetRecordId),
                    buildTaskId);

            UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path("/result/running/{id}");
            int runningBuildId = buildExecutionTask.getId();
            URI uri = uriBuilder.build(runningBuildId);
            return Response.ok(uri).header("location", uri).entity(buildRecordProvider.getSpecificRunning(runningBuildId)).build();
        } catch (BuildConflictException e) {
            return Response.status(Response.Status.CONFLICT).entity(buildRecordProvider.getSpecificRunning(e.getBuildTaskId())).build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Response.serverError().entity("Other error: " + e.getMessage()).build();
        }
    }

    private Set<Integer> parseIntegers(String buildRecordSetIdsCSV) {
        return Arrays.asList(buildRecordSetIdsCSV.split(",")).stream().map((s) -> Integer.parseInt(s)).collect(Collectors.toSet());
    }


}
