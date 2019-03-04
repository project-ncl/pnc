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
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.AuthenticationProviderFactory;
import org.jboss.pnc.auth.LoggedInUser;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.managers.BuildResultPushManager;
import org.jboss.pnc.managers.Result;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordPushResult;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.rest.provider.BuildRecordPushResultProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigSetRecordPushRequestRest;
import org.jboss.pnc.rest.restmodel.BuildRecordPushRequestRest;
import org.jboss.pnc.rest.restmodel.BuildRecordPushResultRest;
import org.jboss.pnc.rest.restmodel.response.ResultRest;
import org.jboss.pnc.rest.restmodel.response.error.ErrorResponseRest;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Hidden
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
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;


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
            BuildConfigurationAuditedRepository buildConfigurationAuditedRepository) {
        super(buildRecordPushResultProvider);
        this.buildResultPushManager = buildResultPushManager;
        this.authenticationProvider = authenticationProviderFactory.getProvider();
        this.buildRecordPushResultRepository = buildRecordPushResultRepository;
        this.buildRecordRepository = buildRecordRepository;
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
        try {
            String pncBaseUrl = StringUtils.stripEndingSlash(configuration.getGlobalConfig().getPncUrl());
            pncRestBaseUrl = StringUtils.stripEndingSlash(pncBaseUrl);
        } catch (ConfigurationParseException e) {
            logger.error("There is a problem while parsing system configuration. Using defaults.", e);
        }
    }

    @POST
    public Response push(
            BuildRecordPushRequestRest buildRecordPushRequestRest,
            @Context UriInfo uriInfo,
            @Context HttpServletRequest httpServletRequest) throws RestValidationException, ProcessException {

        LoggedInUser loginInUser = authenticationProvider.getLoggedInUser(httpServletRequest);

        Integer buildRecordId = buildRecordPushRequestRest.getBuildRecordId();
        BuildRecord buildRecord = buildRecordRepository.queryById(buildRecordId);
        if (buildRecord == null) {
            return Response.noContent().entity("Cannot find a BuildRecord with given id.").build();
        }
        Map<Integer, IdRev> buildRecordsIds = Collections.singletonMap(buildRecordId, buildRecord.getBuildConfigurationAuditedIdRev());
        logger.debug("Pushing BuildRecords {}.", buildRecordsIds);
        Set<Result> pushed = buildResultPushManager.push(
                buildRecordsIds.keySet(),
                loginInUser.getTokenString(),
                getCompleteCallbackUrl(),
                buildRecordPushRequestRest.getTagPrefix());
        logger.info("Push Results {}.", pushed.stream().map(r -> r.getId()).collect(Collectors.joining(",")));
        Set<ResultRest> pushedResponse = toResultRests(pushed, buildRecordsIds);

        return Response.ok().entity(JsonOutputConverterMapper.apply(pushedResponse)).build();
    }

    @POST
    @Path("/record-set/")
    public Response pushRecordSet(
            BuildConfigSetRecordPushRequestRest buildConfigSetRecordPushRequestRest,
            @Context UriInfo uriInfo,
            @Context HttpServletRequest httpServletRequest) throws RestValidationException, ProcessException {

        LoggedInUser loginInUser = authenticationProvider.getLoggedInUser(httpServletRequest);

        List<BuildRecord> buildRecords = buildRecordRepository.queryWithPredicates(
                BuildRecordPredicates.withBuildConfigSetRecordId(buildConfigSetRecordPushRequestRest.getBuildConfigSetRecordId()));

        Map<Integer, IdRev> buildRecordsIds = buildRecords.stream()
                .collect(Collectors.toMap(
                        BuildRecord::getId,
                        BuildRecord::getBuildConfigurationAuditedIdRev
                ));

        Set<Result> pushed = buildResultPushManager.push(
                buildRecordsIds.keySet(),
                loginInUser.getTokenString(),
                getCompleteCallbackUrl(),
                buildConfigSetRecordPushRequestRest.getTagPrefix());

        Set<ResultRest> pushedResponse = toResultRests(pushed, buildRecordsIds);

        return Response.ok().entity(JsonOutputConverterMapper.apply(pushedResponse)).build();
    }

    private Set<ResultRest> toResultRests(Set<Result> pushed, Map<Integer, IdRev> buildRecordsIds) {
        return pushed.stream()
                    .map(r -> createResultRest(r, buildRecordsIds.get(Integer.parseInt(r.getId()))))
                    .collect(Collectors.toSet());
    }

    private ResultRest createResultRest(Result result, IdRev configurationIdRev) {
        return new ResultRest(
                result.getId(),
                getBuildConfigurationName(configurationIdRev),
                ResultRest.Status.valueOf(result.getStatus().name()),
                result.getMessage());
    }

    private String getBuildConfigurationName(IdRev idRev) {
        BuildConfigurationAudited buildConfiguration = buildConfigurationAuditedRepository.queryById(idRev);
        if (buildConfiguration != null) {
            return buildConfiguration.getName();
        } else {
            logger.warn("Did not find buildConfiguration audited by idRev: {}.", idRev);
            return null;
        }
    }

    @GET
    @Path("/{buildRecordPushResultId}")
    public Response get(
            @PathParam("buildRecordId") Integer buildRecordPushResultId
    ) throws RestValidationException, ProcessException {
        return getSpecific(buildRecordPushResultId);
    }

    @POST
    @Path("/{buildRecordId}/cancel/")
    public Response cancel(
            BuildRecordPushResultRest buildRecordPushResult,
            @PathParam("buildRecordId") Integer buildRecordId,
            @Context UriInfo uriInfo) throws RestValidationException, ProcessException {
        boolean canceled = buildResultPushManager.cancelInProgressPush(buildRecordId);
        if (canceled) {
            return Response.ok().build();
        } else {
            return Response.noContent().build();
        }
    }

    @POST
    @Path("/{buildRecordId}/complete/")
    public Response push(
            BuildRecordPushResultRest buildRecordPushResult,
            @PathParam("buildRecordId") Integer buildRecordId,
            @Context UriInfo uriInfo) throws RestValidationException, ProcessException {
        logger.info("Received completion notification for BuildRecord.id: {}. Object received: {}.", buildRecordId, buildRecordPushResult);
        Integer id = buildResultPushManager.complete(buildRecordId, buildRecordPushResult.toDBEntityBuilder().build());
        return Response.ok().entity(id).build();
    }

    @GET
    @Path("/status/{buildRecordId}")
    public Response status(
            @PathParam("buildRecordId") Integer buildRecordId)
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
