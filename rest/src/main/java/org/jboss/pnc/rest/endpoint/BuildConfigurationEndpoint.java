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
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.configuration.BuildConfigurationSupportedGenericParameters;
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.provider.BuildConfigurationSetProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.provider.ProductVersionProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationAuditedRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.ProductVersionRest;
import org.jboss.pnc.rest.restmodel.UserRest;
import org.jboss.pnc.rest.restmodel.response.Singleton;
import org.jboss.pnc.rest.trigger.BuildTriggerer;
import org.jboss.pnc.rest.utils.EndpointAuthenticationProvider;
import org.jboss.pnc.rest.utils.ParameterBackCompatibility;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.enums.RebuildMode;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
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
import java.util.Optional;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.QUERY_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SORTING_QUERY_PARAM;

@Hidden
@Path("/build-configurations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildConfigurationEndpoint extends AbstractEndpoint<BuildConfiguration, BuildConfigurationRest> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private BuildConfigurationProvider buildConfigurationProvider;
    private BuildConfigurationSetProvider buildConfigurationSetProvider;
    private BuildTriggerer buildTriggerer;
    private BuildRecordProvider buildRecordProvider;
    private ProductVersionProvider productVersionProvider;
    private EndpointAuthenticationProvider authenticationProvider;
    @Context
    private HttpServletRequest httpServletRequest;

    private java.util.Map<String, String> buildConfigurationSupportedGenericParameters;

    public static void checkBuildOptionsValidity(BuildOptions buildOptions) throws InvalidEntityException {
        if (!buildOptions.isTemporaryBuild() && buildOptions.isTimestampAlignment()) {
            // Combination timestampAlignment + standard build is not allowed
            throw new InvalidEntityException(
                    "Combination of the build parameters is not allowed. Timestamp alignment is allowed only for temporary builds. ");
        }
    }

    public BuildConfigurationEndpoint() {
    }

    @Inject
    public BuildConfigurationEndpoint(
            BuildConfigurationProvider buildConfigurationProvider,
            BuildConfigurationSetProvider buildConfigurationSetProvider,
            BuildTriggerer buildTriggerer,
            BuildRecordProvider buildRecordProvider,
            ProductVersionProvider productVersionProvider,
            EndpointAuthenticationProvider authenticationProvider,
            BuildConfigurationSupportedGenericParameters supportedGenericParameters) {

        super(buildConfigurationProvider);
        this.buildConfigurationProvider = buildConfigurationProvider;
        this.buildConfigurationSetProvider = buildConfigurationSetProvider;
        this.buildTriggerer = buildTriggerer;
        this.buildRecordProvider = buildRecordProvider;
        this.productVersionProvider = productVersionProvider;
        this.authenticationProvider = authenticationProvider;

        this.buildConfigurationSupportedGenericParameters = supportedGenericParameters.getSupportedGenericParameters();
    }

    @GET
    public Response getAll(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q) {
        return fromCollection(buildConfigurationProvider.getAllNonArchived(pageIndex, pageSize, sort, q));
    }

    @POST
    public Response createNew(BuildConfigurationRest buildConfigurationRest, @Context UriInfo uriInfo)
            throws RestValidationException {
        User currentUser = getCurrentUser();
        buildConfigurationRest.setCreationUser(new UserRest(currentUser));
        buildConfigurationRest.setLastModificationUser(new UserRest(currentUser));
        return super.createNew(buildConfigurationRest, uriInfo);
    }

    @GET
    @Path("/supported-generic-parameters")
    public Response getSupportedGenericParameters() {
        return Response.ok().entity(buildConfigurationSupportedGenericParameters).build();
    }

    @GET
    @Path("/{id}")
    public Response getSpecific(@PathParam("id") Integer id) {
        return super.getSpecific(id);
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, BuildConfigurationRest buildConfigurationRest)
            throws RestValidationException {
        User currentUser = getCurrentUser();
        buildConfigurationRest.setLastModificationUser(new UserRest(currentUser));
        return super.update(id, buildConfigurationRest);
    }

    @POST
    @Path("/{id}/update-and-get-audited")
    public Response updateAndGetAudited(@PathParam("id") Integer id, BuildConfigurationRest buildConfigurationRest)
            throws RestValidationException {
        User currentUser = getCurrentUser();
        buildConfigurationRest.setLastModificationUser(new UserRest(currentUser));
        buildConfigurationProvider.update(id, buildConfigurationRest);
        Optional<BuildConfigurationAuditedRest> buildConfigurationAuditedRestOptional = buildConfigurationProvider
                .getLatestAuditedMatchingBCRest(buildConfigurationRest);

        if (buildConfigurationAuditedRestOptional.isPresent()) {
            return fromSingleton(buildConfigurationAuditedRestOptional.get());
        } else {
            throw new RuntimeException(
                    "Couldn't find updated BuildConfigurationAudited entity. BuildConfigurationRest to be stored: "
                            + buildConfigurationRest);
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteSpecific(@PathParam("id") Integer id) throws RestValidationException {
        User currentUser = getCurrentUser();
        buildConfigurationProvider.archive(id, currentUser);
        return Response.ok().build();
    }

    @POST
    @Path("/{id}/clone")
    public Response clone(@PathParam("id") Integer id, @Context UriInfo uriInfo) throws RestValidationException {
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path("/build-configurations/{id}");
        User currentUser = getCurrentUser();
        int newId = buildConfigurationProvider.clone(id, currentUser);
        return Response.created(uriBuilder.build(newId))
                .entity(new Singleton(buildConfigurationProvider.getSpecific(newId)))
                .build();
    }

    @POST
    @Path("/{id}/build")
    public Response trigger(
            @PathParam("id") Integer id,
            @QueryParam("callbackUrl") String callbackUrl,
            @QueryParam("temporaryBuild") @DefaultValue("false") boolean temporaryBuild,
            @QueryParam("forceRebuild") @DefaultValue("false") boolean forceRebuild,
            @QueryParam("buildDependencies") @DefaultValue("true") boolean buildDependencies,
            @QueryParam("keepPodOnFailure") @DefaultValue("false") boolean keepPodOnFailure,
            @QueryParam("timestampAlignment") @DefaultValue("false") boolean timestampAlignment,
            @QueryParam("rebuildMode") RebuildMode rebuildMode,
            @Context UriInfo uriInfo)
            throws InvalidEntityException, MalformedURLException, BuildConflictException, CoreException {

        rebuildMode = ParameterBackCompatibility.getRebuildMode(forceRebuild, rebuildMode);
        return triggerBuild(
                id,
                Optional.empty(),
                callbackUrl,
                temporaryBuild,
                rebuildMode,
                buildDependencies,
                keepPodOnFailure,
                timestampAlignment,
                uriInfo);
    }

    @POST
    @Path("/{id}/revisions/{rev}/build")
    public Response triggerAudited(
            @PathParam("id") Integer id,
            @PathParam("rev") Integer rev,
            @QueryParam("callbackUrl") String callbackUrl,
            @QueryParam("temporaryBuild") @DefaultValue("false") boolean temporaryBuild,
            @QueryParam("forceRebuild") @DefaultValue("false") boolean forceRebuild,
            @QueryParam("buildDependencies") @DefaultValue("true") boolean buildDependencies,
            @QueryParam("keepPodOnFailure") @DefaultValue("false") boolean keepPodOnFailure,
            @QueryParam("timestampAlignment") @DefaultValue("false") boolean timestampAlignment,
            @QueryParam("rebuildMode") RebuildMode rebuildMode,
            @Context UriInfo uriInfo)
            throws InvalidEntityException, MalformedURLException, BuildConflictException, CoreException {
        rebuildMode = ParameterBackCompatibility.getRebuildMode(forceRebuild, rebuildMode);
        return triggerBuild(
                id,
                Optional.of(rev),
                callbackUrl,
                temporaryBuild,
                rebuildMode,
                buildDependencies,
                keepPodOnFailure,
                timestampAlignment,
                uriInfo);
    }

    private Response triggerBuild(
            Integer id,
            Optional<Integer> rev,
            String callbackUrl,
            boolean temporaryBuild,
            RebuildMode rebuildMode,
            boolean buildDependencies,
            boolean keepPodOnFailure,
            boolean timestampAlignment,
            UriInfo uriInfo)
            throws InvalidEntityException, BuildConflictException, CoreException, MalformedURLException {
        logger.debug(
                "Endpoint /build requested for buildConfigurationId: {}, revision: {}, temporaryBuild: {}, rebuildMode: {}, "
                        + "buildDependencies: {}, keepPodOnFailure: {}, timestampAlignment: {}",
                id,
                rev,
                temporaryBuild,
                rebuildMode,
                buildDependencies,
                keepPodOnFailure,
                timestampAlignment);

        User currentUser = getCurrentUser();

        BuildOptions buildOptions = new BuildOptions(
                temporaryBuild,
                buildDependencies,
                keepPodOnFailure,
                timestampAlignment,
                rebuildMode);
        checkBuildOptionsValidity(buildOptions);

        Integer runningBuildId = null;
        // if callbackUrl is provided trigger build accordingly
        if (callbackUrl != null && !callbackUrl.isEmpty()) {
            logger.debug(
                    "Triggering build for buildConfigurationId {}, rev {} with callback URL {}.",
                    id,
                    rev,
                    callbackUrl);
            runningBuildId = buildTriggerer.triggerBuild(id, rev, currentUser, buildOptions, new URL(callbackUrl));
        } else {
            logger.debug("Triggering build for buildConfigurationId {}, rev {} without callback URL.", id, rev);
            runningBuildId = buildTriggerer.triggerBuild(id, rev, currentUser, buildOptions);
        }

        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path("/build-config-set-records/{id}");
        URI uri = uriBuilder.build(runningBuildId);
        return Response.ok(uri)
                .header("location", uri)
                .entity(new Singleton<>(buildRecordProvider.getSpecificRunning(runningBuildId)))
                .build();
    }

    private User getCurrentUser() throws InvalidEntityException {
        User currentUser = authenticationProvider.getCurrentUser(httpServletRequest);
        if (currentUser == null) {
            throw new InvalidEntityException(
                    "No such user exists to trigger builds or modify build configs. Before triggering builds"
                            + " or modifying build configs, user must be initialized through /users/getLoggedUser");
        }
        return currentUser;
    }

    @GET
    @Path("/projects/{projectId}")
    public Response getAllByProjectId(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q,
            @PathParam("projectId") Integer projectId) {
        return fromCollection(buildConfigurationProvider.getAllForProject(pageIndex, pageSize, sort, q, projectId));
    }

    @GET
    @Path("/products/{productId}")
    public Response getAllByProductId(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q,
            @PathParam("productId") Integer productId) {
        return fromCollection(buildConfigurationProvider.getAllForProduct(pageIndex, pageSize, sort, q, productId));
    }

    @GET
    @Path("/products/{productId}/product-versions/{versionId}")
    public Response getAllByProductVersionId(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q,
            @PathParam("productId") Integer productId,
            @PathParam("versionId") Integer versionId) {
        return fromCollection(
                buildConfigurationProvider
                        .getAllForProductAndProductVersion(pageIndex, pageSize, sort, q, productId, versionId));
    }

    @GET
    @Path("/{id}/dependencies")
    public Response getDependencies(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q,
            @PathParam("id") Integer id) {
        return fromCollection(buildConfigurationProvider.getDependencies(pageIndex, pageSize, sort, q, id));
    }

    @POST
    @Path("/{id}/dependencies")
    public Response addDependency(@PathParam("id") Integer id, BuildConfigurationRest dependency)
            throws RestValidationException {
        User currentUser = getCurrentUser();
        buildConfigurationProvider.addDependency(id, dependency.getId(), currentUser);
        return fromEmpty();
    }

    @DELETE
    @Path("/{id}/dependencies/{dependencyId}")
    public Response removeDependency(@PathParam("id") Integer id, @PathParam("dependencyId") Integer dependencyId)
            throws RestValidationException {
        User currentUser = getCurrentUser();
        buildConfigurationProvider.removeDependency(id, dependencyId, currentUser);
        return fromEmpty();
    }

    @GET
    @Path("/{id}/build-configuration-sets")
    public Response getBuildConfigurationSets(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q,
            @PathParam("id") Integer id) {

        return Response.ok()
                .entity(buildConfigurationSetProvider.getAllForBuildConfiguration(pageIndex, pageSize, sort, q, id))
                .build();
    }

    /**
     * @deprecated use the productVersionId field instead
     */
    @GET
    @Path("/{id}/product-versions")
    @Deprecated
    public Response getProductVersions(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q,
            @PathParam("id") Integer id) {
        return fromCollection(productVersionProvider.getAllForBuildConfiguration(pageIndex, pageSize, sort, q, id));
    }

    /**
     * @deprecated use the productVersionId field instead
     */
    @POST
    @Path("/{id}/product-versions")
    @Deprecated
    public Response addProductVersion(@PathParam("id") Integer id, ProductVersionRest productVersion)
            throws RestValidationException {
        User currentUser = getCurrentUser();
        buildConfigurationProvider.setProductVersion(id, productVersion.getId(), currentUser);
        return fromEmpty();
    }

    /**
     * @deprecated use the productVersionId field instead
     */
    @DELETE
    @Path("/{id}/product-versions/{productVersionId}")
    @Deprecated
    public Response removeProductVersion(
            @PathParam("id") Integer id,
            @PathParam("productVersionId") Integer productVersionId) throws RestValidationException {
        User currentUser = getCurrentUser();
        buildConfigurationProvider.setProductVersion(id, null, currentUser);
        return fromEmpty();
    }

    @GET
    @Path("/{id}/revisions")
    public Response getRevisions(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @PathParam("id") Integer id) {
        return fromCollection(buildConfigurationProvider.getRevisions(pageIndex, pageSize, id));
    }

    @GET
    @Path("/{id}/revisions/{rev}")
    public Response getRevision(@PathParam("id") Integer id, @PathParam("rev") Integer rev) {
        return fromSingleton(buildConfigurationProvider.getRevision(id, rev));
    }

    @POST
    @Path("/{id}/revisions/{rev}/restore")
    public Response restoreRevision(@PathParam("id") Integer id, @PathParam("rev") Integer rev)
            throws RestValidationException {
        User currentUser = getCurrentUser();
        return fromSingleton(buildConfigurationProvider.restoreRevision(id, rev, currentUser));
    }

    @GET
    @Path("/{id}/build-records")
    public Response getBuildRecords(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q,
            @PathParam("id") Integer id) {
        return fromCollection(buildRecordProvider.getAllForBuildConfiguration(pageIndex, pageSize, sort, q, id));
    }

    @GET
    @Path("/{id}/build-records/latest")
    public Response getLatestBuildRecord(@PathParam("id") Integer id, @QueryParam("executed") boolean executed) {
        return this.fromSingleton(buildRecordProvider.getLatestBuildRecord(id, executed));
    }

    // TODO To be removed after testing, will be available via pnc-rest/rest/builds?q=buildConfigurationAuditedId==1
    @GET
    @Path("/{id}/builds")
    public Response getBuilds(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q,
            @PathParam("id") Integer id) {
        return fromCollection(
                buildRecordProvider
                        .getRunningAndCompletedBuildRecordsByBuildConfigurationId(pageIndex, pageSize, sort, q, id));
    }
}
