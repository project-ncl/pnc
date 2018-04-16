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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.AuthenticationProviderFactory;
import org.jboss.pnc.auth.LoggedInUser;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.managers.BuildResultPushManager;
import org.jboss.pnc.managers.Result;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordPushResult;
import org.jboss.pnc.rest.provider.BuildRecordPushResultProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigSetRecordPushRequestRest;
import org.jboss.pnc.rest.restmodel.BuildRecordPushRequestRest;
import org.jboss.pnc.rest.restmodel.BuildRecordPushResultRest;
import org.jboss.pnc.rest.restmodel.response.ResultRest;
import org.jboss.pnc.rest.restmodel.response.error.ErrorResponseRest;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordPushResultRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Api(value = "/build-record-push", description = "BuildRecordPush related information")
@Path("/build-record-push")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildRecordPushEndpoint extends AbstractEndpoint<BuildRecordPushResult, BuildRecordPushResultRest>  {

    private Logger logger = LoggerFactory.getLogger(BuildRecordPushEndpoint.class);

    private BuildResultPushManager buildResultPushManager;
    private AuthenticationProvider authenticationProvider;
    private String pncRestBaseUrl;
    private BuildRecordPushResultRepository buildRecordPushResultRepository;
    private BuildRecordRepository buildRecordRepository;
    private BuildConfigurationRepository buildConfigurationRepository;


    @Context
    private HttpServletRequest httpServletRequest;

    @Deprecated //RestEasy - CDI workaround
    public BuildRecordPushEndpoint() {
    }

    @Inject
    public BuildRecordPushEndpoint(
            BuildRecordPushResultProvider buildRecordPushResultProvider,
            BuildResultPushManager buildResultPushManager,
            AuthenticationProviderFactory authenticationProviderFactory,
            Configuration configuration,
            BuildRecordPushResultRepository buildRecordPushResultRepository,
            BuildRecordRepository buildRecordRepository,
            BuildConfigurationRepository buildConfigurationRepository) {
        super(buildRecordPushResultProvider);
        this.buildResultPushManager = buildResultPushManager;
        this.authenticationProvider = authenticationProviderFactory.getProvider();
        this.buildRecordPushResultRepository = buildRecordPushResultRepository;
        this.buildRecordRepository = buildRecordRepository;
        this.buildConfigurationRepository = buildConfigurationRepository;
        try {
            String pncBaseUrl = StringUtils.stripEndingSlash(configuration.getGlobalConfig().getPncUrl());
            pncRestBaseUrl = StringUtils.stripEndingSlash(pncBaseUrl);
        } catch (ConfigurationParseException e) {
            logger.error("There is a problem while parsing system configuration. Using defaults.", e);
        }
    }

    @ApiOperation(value = "Push build record results to Brew.")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = "Map of all requested BuildRecord ids with boolean status.", response = Map.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    public Response push(
            BuildRecordPushRequestRest buildRecordPushRequestRest,
            @Context UriInfo uriInfo,
            @Context HttpServletRequest httpServletRequest) throws RestValidationException, ProcessException {

        LoggedInUser loginInUser = authenticationProvider.getLoggedInUser(httpServletRequest);

        Integer buildRecordId = buildRecordPushRequestRest.getBuildRecordId();
        Set<Integer> buildRecordIds = new HashSet<>();
        buildRecordIds.add(buildRecordId);

        Set<Result> pushed = buildResultPushManager.push(
                buildRecordIds,
                loginInUser.getTokenString(),
                getCompleteCallbackUrl(),
                buildRecordPushRequestRest.getTagPrefix());

        Set<ResultRest> pushedResponse = toResultRests(pushed);

        return Response.ok().entity(JsonOutputConverterMapper.apply(pushedResponse)).build();
    }

    @ApiOperation(value = "Push build config set record to Brew.")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = "Map of all requested BuildRecord ids with boolean status.", response = Map.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    @Path("/record-set/")
    public Response pushRecordSet(
            BuildConfigSetRecordPushRequestRest buildConfigSetRecordPushRequestRest,
            @Context UriInfo uriInfo,
            @Context HttpServletRequest httpServletRequest) throws RestValidationException, ProcessException {

        LoggedInUser loginInUser = authenticationProvider.getLoggedInUser(httpServletRequest);

        List<BuildRecord> buildRecords = buildRecordRepository.queryWithPredicates(
                BuildRecordPredicates.withBuildConfigSetRecordId(buildConfigSetRecordPushRequestRest.getBuildConfigSetRecordId()));

        Set<Integer> buildRecordIds = buildRecords.stream()
                .map(BuildRecord::getId)
                .collect(Collectors.toSet());

        Set<Result> pushed = buildResultPushManager.push(
                buildRecordIds,
                loginInUser.getTokenString(),
                getCompleteCallbackUrl(),
                buildConfigSetRecordPushRequestRest.getTagPrefix());

        Set<ResultRest> pushedResponse = toResultRests(pushed);

        return Response.ok().entity(JsonOutputConverterMapper.apply(pushedResponse)).build();
    }

    private Set<ResultRest> toResultRests(Set<Result> pushed) {
        return pushed.stream()
                    .map(r -> createResultRest(r))
                    .collect(Collectors.toSet());
    }

    private ResultRest createResultRest(Result result) {
        return new ResultRest(
                result.getId(),
                getBuildConfigurationName(Integer.parseInt(result.getId())),
                ResultRest.Status.valueOf(result.getStatus().name()),
                result.getMessage());
    }

    private String getBuildConfigurationName(Integer buildConfigurationId) {
        return buildConfigurationRepository.queryById(buildConfigurationId).getName();
    }

    @ApiOperation(value = "Get Build Record Push Result by Id..")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildRecordPushResultRest.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{buildRecordPushResultId}")
    public Response get(
            @ApiParam(value = "Build Record id", required = true) @PathParam("buildRecordId") Integer buildRecordPushResultId
    ) throws RestValidationException, ProcessException {
        return getSpecific(buildRecordPushResultId);
    }

    @ApiOperation(value = "Build record push results.")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = Integer.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    @Path("/{buildRecordId}/cancel/")
    public Response cancel(
            BuildRecordPushResultRest buildRecordPushResult,
            @ApiParam(value = "Build Record id", required = true) @PathParam("buildRecordId") Integer buildRecordId,
            @Context UriInfo uriInfo) throws RestValidationException, ProcessException {
        boolean canceled = buildResultPushManager.cancelInProgressPush(buildRecordId);
        if (canceled) {
            return Response.ok().build();
        } else {
            return Response.noContent().build();
        }
    }

    @ApiOperation(value = "Build record push results.")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = Integer.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    @Path("/{buildRecordId}/complete/")
    public Response push(
            BuildRecordPushResultRest buildRecordPushResult,
            @ApiParam(value = "Build Record id", required = true) @PathParam("buildRecordId") Integer buildRecordId,
            @Context UriInfo uriInfo) throws RestValidationException, ProcessException {
        logger.info("Received completion notification for BuildRecord.id: {}. Object received: {}.", buildRecordId, buildRecordPushResult);
        Integer id = buildResultPushManager.complete(buildRecordId, buildRecordPushResult.toDBEntityBuilder().build());
        return Response.ok().entity(id).build();
    }

    @ApiOperation(value = "Latest push result of BuildRecord.")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildRecordPushResultRest.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/status/{buildRecordId}")
    public Response status(
            @ApiParam(value = "Build Record id", required = true) @PathParam("buildRecordId") Integer buildRecordId)
            throws RestValidationException, ProcessException {

        BuildRecordPushResult latestForBuildRecord = buildRecordPushResultRepository.getLatestForBuildRecord(buildRecordId);
        if (latestForBuildRecord != null) {
            BuildRecordPushResultRest buildRecordPushResultRest = new BuildRecordPushResultRest(latestForBuildRecord);
            return Response.ok().entity(buildRecordPushResultRest.toString()).build();
        } else {
            return Response.noContent().build();
        }
    }

    private String getCompleteCallbackUrl() {
        return pncRestBaseUrl + "/build-record-push/%d/complete/";
    }
}
