/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.common.logging.BuildTaskContext;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.User;
import org.jboss.pnc.pncmetrics.rest.TimedMetric;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.trigger.BuildTriggerer;
import org.jboss.pnc.rest.utils.EndpointAuthenticationProvider;
import org.jboss.pnc.spi.SshCredentials;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
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
import java.util.Optional;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.NO_CONTENT_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.QUERY_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SORTING_QUERY_PARAM;

@Hidden
@Path("/builds")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildEndpoint extends AbstractEndpoint<BuildRecord, BuildRecordRest> {

    private static final Logger logger = LoggerFactory.getLogger(BuildEndpoint.class);

    private BuildRecordProvider buildRecordProvider;
    private EndpointAuthenticationProvider endpointAuthProvider;
    private BuildTriggerer buildTriggerer;

    @Context
    private HttpServletRequest request;

    @Deprecated
    public BuildEndpoint() {
    }

    @Inject
    public BuildEndpoint(
            BuildRecordProvider buildRecordProvider,
            EndpointAuthenticationProvider endpointAuthProvider,
            BuildTriggerer buildTriggerer) {
        super(buildRecordProvider);
        this.buildRecordProvider = buildRecordProvider;
        this.endpointAuthProvider = endpointAuthProvider;
        this.buildTriggerer = buildTriggerer;
    }

    @GET
    @TimedMetric
    public Response getAll(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q,
            @QueryParam("orFindByBuildConfigurationName") String orFindByBuildConfigurationName,
            @QueryParam("andFindByBuildConfigurationName") String andFindByBuildConfigurationName) {
        return fromCollection(
                buildRecordProvider.getRunningAndCompletedBuildRecords(
                        pageIndex,
                        pageSize,
                        sort,
                        orFindByBuildConfigurationName,
                        andFindByBuildConfigurationName,
                        q));
    }

    @GET
    @Path("/{id}")
    public Response getSpecific(@PathParam("id") Integer id) {
        BuildRecordRest record = buildRecordProvider.getSpecific(id);
        if (record == null) {
            record = buildRecordProvider.getSpecificRunning(id);
        }
        return fromSingleton(record);
    }

    @GET
    @Path("/ssh-credentials/{id}")
    public Response getSshCredentials(@PathParam("id") Integer id) {
        User currentUser = endpointAuthProvider.getCurrentUser(request);
        if (currentUser == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        SshCredentials credentials = buildRecordProvider.getSshCredentialsForUser(id, currentUser);
        if (credentials != null) {
            return fromSingleton(credentials);
        } else {
            return Response.status(NO_CONTENT_CODE).build();
        }
    }

    @POST
    @Path("/{id}/cancel")
    public Response cancel(@PathParam("id") Integer buildTaskId) {
        boolean success = false;
        try {
            logger.debug("Received cancel request for buildTaskId: {}.", buildTaskId);
            Optional<BuildTaskContext> mdcMeta = buildTriggerer.getMdcMeta(buildTaskId);
            if (mdcMeta.isPresent()) {
                MDCUtils.addContext(mdcMeta.get());
            } else {
                logger.warn("Unable to retrieve MDC meta. There is no running build for buildTaskId: {}.", buildTaskId);
            }
            success = buildTriggerer.cancelBuild(buildTaskId);
        } catch (BuildConflictException | CoreException e) {
            logger.error("Unable to cancel the build [" + buildTaskId + "].", e);
            return Response.serverError().build();
        }
        if (success) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
