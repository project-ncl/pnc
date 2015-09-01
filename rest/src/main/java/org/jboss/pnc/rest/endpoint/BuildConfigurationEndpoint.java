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
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.provider.ConflictedEntryException;
import org.jboss.pnc.rest.provider.ProductVersionProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationAuditedRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.restmodel.ProductVersionRest;
import org.jboss.pnc.rest.trigger.BuildTriggerer;
import org.jboss.pnc.spi.datastore.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URL;

@Api(value = "/build-configurations", description = "Build configuration entities")
@Path("/build-configurations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildConfigurationEndpoint extends AbstractEndpoint<BuildConfiguration, BuildConfigurationRest> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private BuildConfigurationProvider buildConfigurationProvider;
    private BuildTriggerer buildTriggerer;
    private BuildRecordProvider buildRecordProvider;
    private ProductVersionProvider productVersionProvider;
    
    @Context
    private HttpServletRequest httpServletRequest;
    
    @Inject
    private Datastore datastore;

    public BuildConfigurationEndpoint() {
    }

    @Inject
    public BuildConfigurationEndpoint(BuildConfigurationProvider buildConfigurationProvider, BuildTriggerer buildTriggerer,
            BuildRecordProvider buildRecordProvider, ProductVersionProvider productVersionProvider) {
        super(buildConfigurationProvider);
        this.buildConfigurationProvider = buildConfigurationProvider;
        this.buildTriggerer = buildTriggerer;
        this.buildRecordProvider = buildRecordProvider;
        this.productVersionProvider = productVersionProvider;
    }

    @ApiOperation(value = "Gets all Build Configurations",
            responseContainer = "List", response = BuildConfigurationRest.class)
    @GET
    public Response getAll(@ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql) {
        return super.getAll(pageIndex, pageSize, sortingRsql, rsql);
    }

    @ApiOperation(value = "Creates a new Build Configuration", response = BuildConfigurationRest.class)
    @POST
    public Response createNew(@NotNull @Valid BuildConfigurationRest buildConfigurationRest, @Context UriInfo uriInfo) throws ConflictedEntryException {
        return super.createNew(buildConfigurationRest, uriInfo);
    }

    @ApiOperation(value = "Gets a specific Build Configuration", response = BuildConfigurationRest.class)
    @GET
    @Path("/{id}")
    public Response getSpecific(
            @ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id) {
        return super.getSpecific(id);
    }

    @ApiOperation(value = "Updates an existing Build Configuration")
    @PUT
    @Path("/{id}")
    public Response update(@ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id,
            @NotNull @Valid BuildConfigurationRest buildConfigurationRest) throws ConflictedEntryException {
        return super.update(id, buildConfigurationRest);
    }

    @ApiOperation(value = "Removes a specific Build Configuration")
    @DELETE
    @Path("/{id}")
    public Response deleteSpecific(@ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id) {
        return super.delete(id);
    }

    @ApiOperation(value = "Clones an existing Build Configuration", response = BuildConfigurationRest.class)
    @POST
    @Path("/{id}/clone")
    public Response clone(@ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id,
            @Context UriInfo uriInfo) {
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path("/build-configurations/{id}");
        int newId = buildConfigurationProvider.clone(id);
        return Response.created(uriBuilder.build(newId)).entity(buildConfigurationProvider.getSpecific(newId)).build();
    }

    @ApiOperation(value = "Triggers the build of a specific Build Configuration")
    @POST
    @Path("/{id}/build")
    @Consumes(MediaType.WILDCARD)
    public Response trigger(@ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = "Optional Callback URL", required = false) @QueryParam("callbackUrl") String callbackUrl,
            @Context UriInfo uriInfo) {
        try {
            AuthenticationProvider authProvider = new AuthenticationProvider(httpServletRequest);
            String loggedUser = authProvider.getUserName();
            User currentUser = null;
            if(loggedUser != null && loggedUser != "") {
                currentUser = datastore.retrieveUserByUsername(loggedUser);
            }
            if(currentUser == null) {
                currentUser = User.Builder.newBuilder()
                        .username(loggedUser)
                        .firstName(authProvider.getFirstName())
                        .lastName(authProvider.getLastName())
                        .email(authProvider.getEmail()).build();
                datastore.createNewUser(currentUser);
            }
            
            Integer runningBuildId = null;
            // if callbackUrl is provided trigger build accordingly
            if (callbackUrl == null || callbackUrl.isEmpty()) {
                runningBuildId = buildTriggerer.triggerBuilds(id, currentUser);
            } else {
                runningBuildId = buildTriggerer.triggerBuilds(id, currentUser, new URL(callbackUrl));
            }
            
            UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path("/result/running/{id}");
            URI uri = uriBuilder.build(runningBuildId);
            return Response.ok(uri).header("location", uri).entity(buildRecordProvider.getSpecificRunning(runningBuildId)).build();
        } catch (CoreException e) {
            logger.error(e.getMessage(), e);
            return Response.serverError().entity("Core error: " + e.getMessage()).build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Response.serverError().entity("Other error: " + e.getMessage()).build();
        }
    }

    @ApiOperation(value = "Gets all Build Configurations of a Project",
            responseContainer = "List", response = BuildConfigurationRest.class)
    @GET
    @Path("/projects/{projectId}")
    public Response getAllByProjectId(@ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql,
            @ApiParam(value = "Project id", required = true) @PathParam("projectId") Integer projectId) {
        return fromCollection(buildConfigurationProvider.getAllForProject(pageIndex, pageSize, sortingRsql, rsql, projectId));
    }

    @ApiOperation(value = "Gets all Build Configurations of a Product",
            responseContainer = "List", response = BuildConfigurationRest.class)
    @GET
    @Path("/products/{productId}")
    public Response getAllByProductId(@ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql,
            @ApiParam(value = "Product id", required = true) @PathParam("productId") Integer productId) {
        return fromCollection(buildConfigurationProvider.getAllForProduct(pageIndex, pageSize, sortingRsql, rsql, productId));
    }

    @ApiOperation(value = "Gets all Build Configurations of the Specified Product Version",
            responseContainer = "List", response = BuildConfigurationRest.class)
    @GET
    @Path("/products/{productId}/product-versions/{versionId}")
    public Response getAllByProductId(@ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql,
            @ApiParam(value = "Product id", required = true) @PathParam("productId") Integer productId,
            @ApiParam(value = "Product Version id", required = true) @PathParam("versionId") Integer versionId) {
        return fromCollection(buildConfigurationProvider
                .getAllForProductAndProductVersion(pageIndex, pageSize, sortingRsql, rsql, productId, versionId));
    }

    @ApiOperation(value = "Get the direct dependencies of the specified configuration",
            responseContainer = "List", response = BuildConfigurationRest.class)
    @GET
    @Path("/{id}/dependencies")
    public Response getDependencies(@ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql,
            @ApiParam(value = "Build configuration id", required = true) @PathParam("id") Integer id) {
        return fromCollection(buildConfigurationProvider.getDependencies(pageIndex, pageSize, sortingRsql, rsql, id));
    }

    @ApiOperation(value = "Adds a dependency to the specified config")
    @POST
    @Path("/{id}/dependencies")
    public Response addDependency(@ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id,
            BuildConfigurationRest dependency) {
        buildConfigurationProvider.addDependency(id, dependency.getId());
        return fromEmpty();
    }

    @ApiOperation(value = "Removes a configuration from the specified config set")
    @DELETE
    @Path("/{id}/dependencies/{dependencyId}")
    public Response removeDependency(
            @ApiParam(value = "Build configuration set id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = "Build configuration id", required = true) @PathParam("dependencyId") Integer dependencyId) {
        buildConfigurationProvider.removeDependency(id, dependencyId);
        return fromEmpty();
    }

    @ApiOperation(value = "Get associated Product Versions of the specified Configuration",
            responseContainer = "List", response = ProductVersionRest.class)
    @GET
    @Path("/{id}/product-versions")
    public Response getProductVersions(@ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql,
            @ApiParam(value = "Build configuration id", required = true) @PathParam("id") Integer id) {
        return fromCollection(productVersionProvider.getAllForBuildConfiguration(pageIndex, pageSize, sortingRsql, rsql, id));
    }

    @ApiOperation(value = "Associates a product version to the specified config")
    @POST
    @Path("/{id}/product-versions")
    public Response addProductVersion(
            @ApiParam(value = "Build Configuration id", required = true) @PathParam("id") Integer id,
            ProductVersionRest productVersion) {
        buildConfigurationProvider.addProductVersion(id, productVersion.getId());
        return fromEmpty();
    }

    @ApiOperation(value = "Removes a product version from the specified config set")
    @DELETE
    @Path("/{id}/product-versions/{productVersionId}")
    public Response removeProductVersion(
            @ApiParam(value = "Build configuration set id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = "Product version id", required = true) @PathParam("productVersionId") Integer productVersionId) {
        buildConfigurationProvider.removeProductVersion(id, productVersionId);
        return fromEmpty();
    }

    @ApiOperation(value = "Gets audited revisions of this build configuration",
            responseContainer = "List", response = BuildConfigurationAuditedRest.class)
    @GET
    @Path("/{id}/revisions")
    public Response getRevisions(@ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Build configuration id", required = true) @PathParam("id") Integer id) {
        return fromCollection(buildConfigurationProvider.getRevisions(pageIndex, pageSize, id));
    }

    @ApiOperation(value = "Get specific audited revision of this build configuration",
            response = BuildConfigurationAuditedRest.class)
    @GET
    @Path("/{id}/revisions/{rev}")
    public Response getRevision(@ApiParam(value = "Build configuration id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = "Build configuration rev", required = true) @PathParam("rev") Integer rev) {
        return fromSingleton(buildConfigurationProvider.getRevision(id, rev));
    }

    @ApiOperation(value = "Get all build record associated with this build configuration, returns empty list if no build records are found",
            responseContainer = "List", response = BuildRecordRest.class)
    @GET
    @Path("/{id}/build-records")
    public Response getBuildRecords(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql,
            @ApiParam(value = "Build configuration id", required = true) @PathParam("id") Integer id) {
        return fromCollection(buildRecordProvider.getAllForBuildConfiguration(pageIndex, pageSize, sortingRsql, rsql, id));
    }

    @ApiOperation(value = "Get latest build record associated with this build configuration, returns no content if no build records are found",
            response = BuildRecordRest.class)
    @GET
    @Path("/{id}/build-records/latest")
    public Response getLatestBuildRecord(@ApiParam(value = "Build configuration id", required = true) @PathParam("id") Integer id) {
        return fromSingleton(buildRecordProvider.getLatestBuildRecord(id));
    }

}
