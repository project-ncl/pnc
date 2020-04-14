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
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.provider.BuildConfigSetRecordProvider;
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.provider.BuildConfigurationSetProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetWithAuditedBCsRest;
import org.jboss.pnc.rest.restmodel.response.Singleton;
import org.jboss.pnc.rest.trigger.BuildConfigurationSetTriggerResult;
import org.jboss.pnc.rest.trigger.BuildTriggerer;
import org.jboss.pnc.rest.utils.EndpointAuthenticationProvider;
import org.jboss.pnc.rest.utils.ParameterBackCompatibility;
import org.jboss.pnc.rest.validation.exceptions.EmptyEntityException;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.enums.RebuildMode;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.QUERY_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SORTING_QUERY_PARAM;

@Hidden
@Path("/build-configuration-sets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildConfigurationSetEndpoint extends AbstractEndpoint<BuildConfigurationSet, BuildConfigurationSetRest> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private BuildTriggerer buildTriggerer;

    @Context
    private HttpServletRequest httpServletRequest;

    private Datastore datastore;
    private EndpointAuthenticationProvider endpointAuthProvider;
    private BuildConfigurationSetProvider buildConfigurationSetProvider;
    private BuildConfigurationProvider buildConfigurationProvider;
    private BuildRecordProvider buildRecordProvider;
    private BuildConfigSetRecordProvider buildConfigSetRecordProvider;

    public BuildConfigurationSetEndpoint() {

    }

    @Inject
    public BuildConfigurationSetEndpoint(
            BuildConfigurationSetProvider buildConfigurationSetProvider,
            BuildTriggerer buildTriggerer,
            BuildConfigurationProvider buildConfigurationProvider,
            BuildRecordProvider buildRecordProvider,
            BuildConfigSetRecordProvider buildConfigSetRecordProvider,
            Datastore datastore,
            EndpointAuthenticationProvider endpointAuthProvider) {
        super(buildConfigurationSetProvider);
        this.buildConfigurationSetProvider = buildConfigurationSetProvider;
        this.buildTriggerer = buildTriggerer;
        this.buildConfigurationProvider = buildConfigurationProvider;
        this.buildRecordProvider = buildRecordProvider;
        this.buildConfigSetRecordProvider = buildConfigSetRecordProvider;
        this.endpointAuthProvider = endpointAuthProvider;
        this.datastore = datastore;
    }

    @GET
    public Response getAll(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q) {
        return fromCollection(buildConfigurationSetProvider.getAllNonArchived(pageIndex, pageSize, sort, q));
    }

    @POST
    public Response createNew(@NotNull BuildConfigurationSetRest buildConfigurationSetRest, @Context UriInfo uriInfo)
            throws RestValidationException {
        logger.debug("Creating new BuildConfigurationSet: {}", buildConfigurationSetRest.toString());
        return super.createNew(buildConfigurationSetRest, uriInfo);
    }

    @GET
    @Path("/{id}")
    public Response getSpecific(@PathParam("id") Integer id) {
        return super.getSpecific(id);
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, @NotNull BuildConfigurationSetRest buildConfigurationSetRest)
            throws RestValidationException {
        return super.update(id, buildConfigurationSetRest);
    }

    @DELETE
    @Path("/{id}")
    public Response deleteSpecific(@PathParam("id") Integer id) throws RestValidationException {
        buildConfigurationSetProvider.deleteOrArchive(id);
        return Response.ok().build();
    }

    @GET
    @Path("/{id}/build-configurations")
    public Response getConfigurations(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q,
            @PathParam("id") Integer id) {
        return fromCollection(
                buildConfigurationProvider.getAllForBuildConfigurationSet(pageIndex, pageSize, sort, q, id));
    }

    @PUT
    @Path("/{id}/build-configurations")
    public Response updateConfigurations(
            @PathParam("id") Integer id,
            List<BuildConfigurationRest> buildConfigurationRests) throws RestValidationException {
        buildConfigurationSetProvider.updateConfigurations(id, buildConfigurationRests);
        return Response.ok().build();
    }

    @POST
    @Path("/{id}/build-configurations")
    public Response addConfiguration(@PathParam("id") Integer id, BuildConfigurationRest buildConfig)
            throws RestValidationException {
        if (buildConfig == null || buildConfig.getId() == null) {
            throw new EmptyEntityException("No valid build config included in request to add config to set id: " + id);
        }
        buildConfigurationSetProvider.addConfiguration(id, buildConfig.getId());
        return fromEmpty();
    }

    @DELETE
    @Path("/{id}/build-configurations/{configId}")
    public Response removeConfiguration(@PathParam("id") Integer id, @PathParam("configId") Integer configId)
            throws RestValidationException {
        buildConfigurationSetProvider.removeConfiguration(id, configId);
        return fromEmpty();
    }

    @GET
    @Path("/{id}/build-records")
    public Response getBuildRecords(
            @PathParam("id") Integer id,
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q) {
        return fromCollection(buildRecordProvider.getAllForBuildConfigSet(pageIndex, pageSize, sort, q, id));
    }

    @POST
    @Path("/{id}/build")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response build(
            @PathParam("id") Integer id,
            @QueryParam("callbackUrl") String callbackUrl,
            @QueryParam("temporaryBuild") @DefaultValue("false") boolean temporaryBuild,
            @QueryParam("forceRebuild") @DefaultValue("false") boolean forceRebuild,
            @QueryParam("timestampAlignment") @DefaultValue("false") boolean timestampAlignment,
            @QueryParam("rebuildMode") RebuildMode rebuildMode,
            @Context UriInfo uriInfo) throws CoreException, MalformedURLException, InvalidEntityException {
        logger.info(
                "Executing build configuration set: [id: {}, temporaryBuild: {}, forceRebuild: {}, timestampAlignment: {}, rebuildMode: {}.]",
                id,
                temporaryBuild,
                forceRebuild,
                timestampAlignment,
                rebuildMode);
        rebuildMode = ParameterBackCompatibility.getRebuildMode(forceRebuild, rebuildMode);
        return triggerBuild(
                Optional.empty(),
                Optional.of(id),
                callbackUrl,
                temporaryBuild,
                rebuildMode,
                timestampAlignment,
                uriInfo);
    }

    @POST
    @Path("/{id}/build-versioned")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response buildVersioned(
            @PathParam("id") Integer id,
            @QueryParam("callbackUrl") String callbackUrl,
            @QueryParam("temporaryBuild") @DefaultValue("false") boolean temporaryBuild,
            @QueryParam("forceRebuild") @DefaultValue("false") boolean forceRebuild,
            @QueryParam("timestampAlignment") @DefaultValue("false") boolean timestampAlignment,
            BuildConfigurationSetWithAuditedBCsRest buildConfigurationAuditedRest,
            @QueryParam("rebuildMode") RebuildMode rebuildMode,
            @Context UriInfo uriInfo) throws CoreException, MalformedURLException, InvalidEntityException {
        logger.info(
                "Executing build configuration set with build configurations in specific values: "
                        + "[BuildConfigurationSetAuditedRest: {}, temporaryBuild: {}, forceRebuild: {}, timestampAlignment: {}, rebuildMode: {}.]",
                buildConfigurationAuditedRest,
                temporaryBuild,
                forceRebuild,
                timestampAlignment,
                rebuildMode);

        rebuildMode = ParameterBackCompatibility.getRebuildMode(forceRebuild, rebuildMode);
        return triggerBuild(
                Optional.of(buildConfigurationAuditedRest),
                Optional.empty(),
                callbackUrl,
                temporaryBuild,
                rebuildMode,
                timestampAlignment,
                uriInfo);
    }

    private Response triggerBuild(
            Optional<BuildConfigurationSetWithAuditedBCsRest> buildConfigurationAuditedRest,
            Optional<Integer> buildConfigurationSetId,
            String callbackUrl,
            boolean temporaryBuild,
            RebuildMode rebuildMode,
            boolean timestampAlignment,
            UriInfo uriInfo) throws InvalidEntityException, CoreException, MalformedURLException {
        User currentUser = getCurrentUser();

        BuildOptions buildOptions = new BuildOptions(temporaryBuild, false, false, timestampAlignment, rebuildMode);
        BuildConfigurationEndpoint.checkBuildOptionsValidity(buildOptions);

        BuildConfigurationSetTriggerResult result;
        if (buildConfigurationSetId.isPresent()) {
            if (callbackUrl != null && !callbackUrl.isEmpty()) {
                result = buildTriggerer.triggerBuildConfigurationSet(
                        buildConfigurationSetId.get(),
                        currentUser,
                        buildOptions,
                        new URL(callbackUrl));
            } else {
                result = buildTriggerer
                        .triggerBuildConfigurationSet(buildConfigurationSetId.get(), currentUser, buildOptions);
            }
        } else {
            if (callbackUrl != null && !callbackUrl.isEmpty()) {
                result = buildTriggerer.triggerBuildConfigurationSet(
                        buildConfigurationAuditedRest.get(),
                        currentUser,
                        buildOptions,
                        new URL(callbackUrl));
            } else {
                result = buildTriggerer
                        .triggerBuildConfigurationSet(buildConfigurationAuditedRest.get(), currentUser, buildOptions);
            }
        }

        logger.info(
                "Started build BuildConfigurationSetAuditedRest: {}. Build Tasks: {}",
                buildConfigurationAuditedRest,
                result.getBuildTasks().stream().map(bt -> Integer.toString(bt.getId())).collect(Collectors.joining()));

        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path("/build-config-set-records/{id}");
        URI uri = uriBuilder.build(result.getBuildRecordSetId());
        return Response.ok(uri)
                .header("location", uri)
                .entity(new Singleton<>(buildConfigSetRecordProvider.getSpecific(result.getBuildRecordSetId())))
                .build();
    }

    private User getCurrentUser() throws InvalidEntityException {
        User currentUser = endpointAuthProvider.getCurrentUser(httpServletRequest);
        if (currentUser == null) {
            throw new InvalidEntityException(
                    "No such user exists to trigger builds. Before triggering builds"
                            + " user must be initialized through /users/getLoggedUser");
        }
        return currentUser;
    }

    @GET
    @Path("/{id}/build-config-set-records")
    public Response getAllBuildConfigSetRecords(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q,
            @PathParam("id") Integer id) {
        return fromCollection(buildConfigSetRecordProvider.getAllForBuildConfigSet(pageIndex, pageSize, sort, q, id));
    }

}
