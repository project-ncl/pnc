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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.AuthenticationProviderFactory;
import org.jboss.pnc.auth.LoggedInUser;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.rest.provider.ArtifactProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.provider.ProductMilestoneProvider;
import org.jboss.pnc.rest.provider.ProductMilestoneReleaseProvider;
import org.jboss.pnc.rest.restmodel.ArtifactRest;
import org.jboss.pnc.rest.restmodel.ProductMilestoneReleaseRest;
import org.jboss.pnc.rest.restmodel.ProductMilestoneRest;
import org.jboss.pnc.rest.restmodel.response.Singleton;
import org.jboss.pnc.rest.restmodel.response.error.ErrorResponseRest;
import org.jboss.pnc.rest.swagger.response.ArtifactPage;
import org.jboss.pnc.rest.swagger.response.BuildRecordPage;
import org.jboss.pnc.rest.swagger.response.ProductMilestonePage;
import org.jboss.pnc.rest.swagger.response.ProductMilestoneReleaseSingleton;
import org.jboss.pnc.rest.swagger.response.ProductMilestoneSingleton;
import org.jboss.pnc.rest.validation.exceptions.EmptyEntityException;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;

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
import javax.ws.rs.core.UriInfo;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NO_CONTENT_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NO_CONTENT_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.QUERY_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.QUERY_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SORTING_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SORTING_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withDistributedInProductMilestone;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withPerformedInMilestone;

@Api(value = "/product-milestones", description = "Product Milestone related information")
@Path("/product-milestones")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductMilestoneEndpoint extends AbstractEndpoint<ProductMilestone, ProductMilestoneRest> {

    private ArtifactProvider artifactProvider;
    private BuildRecordProvider buildRecordProvider;
    private ProductMilestoneProvider productMilestoneProvider;
    private ProductMilestoneReleaseProvider milestoneReleaseProvider;
    private AuthenticationProvider authenticationProvider;

    private ProductMilestoneRepository milestoneRepository;

    public ProductMilestoneEndpoint() {
    }

    @Inject
    public ProductMilestoneEndpoint(
            ProductMilestoneProvider productMilestoneProvider,
            ArtifactProvider artifactProvider,
            BuildRecordProvider buildRecordProvider,
            ProductMilestoneReleaseProvider milestoneReleaseProvider,
            AuthenticationProviderFactory authenticationProviderFactory,
            ProductMilestoneRepository milestoneRepository) {

        super(productMilestoneProvider);
        this.productMilestoneProvider = productMilestoneProvider;
        this.artifactProvider = artifactProvider;
        this.buildRecordProvider = buildRecordProvider;
        this.milestoneReleaseProvider = milestoneReleaseProvider;
        this.authenticationProvider = authenticationProviderFactory.getProvider();
        this.milestoneRepository = milestoneRepository;

    }

    @ApiOperation(value = "Gets all Product Milestones")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = ProductMilestonePage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = ProductMilestonePage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    public Response getAll(@ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q) {
        return super.getAll(pageIndex, pageSize, sort, q);
    }

    @ApiOperation(value = "Gets all Product Milestones of the Specified Product Version")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = ProductMilestonePage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = ProductMilestonePage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/product-versions/{versionId}")
    public Response getAllByProductVersionId(
            @ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q,
            @ApiParam(value = "Product Version id", required = true) @PathParam("versionId") Integer versionId) {
        return fromCollection(productMilestoneProvider.getAllForProductVersion(pageIndex, pageSize, sort, q, versionId));
    }

    @ApiOperation(value = "Gets specific Product Milestone")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = ProductMilestoneSingleton.class),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION, response = ProductMilestoneSingleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}")
    public Response getSpecific(
            @ApiParam(value = "Product Milestone id", required = true) @PathParam("id") Integer id) {
        return super.getSpecific(id);
    }

    @ApiOperation(value = "Creates a new Product Milestone for the Specified Product Version")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = ProductMilestoneSingleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    public Response createNew(ProductMilestoneRest productMilestoneRest, @Context UriInfo uriInfo)
            throws RestValidationException {
        return super.createNew(productMilestoneRest, uriInfo);
    }

    @ApiOperation(value = "Updates an existing Product Milestone")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @PUT
    @Path("/{id}")
    public Response update(
            @ApiParam(value = "Product Milestone id", required = true) @PathParam("id") Integer id,
            ProductMilestoneRest productMilestoneRest,
            @Context UriInfo uriInfo,
            @Context HttpServletRequest httpServletRequest) throws RestValidationException {

        LoggedInUser loginInUser = authenticationProvider.getLoggedInUser(httpServletRequest);

        productMilestoneProvider.update(id, productMilestoneRest, loginInUser.getTokenString());
        return Response.ok().build();
    }

    @ApiOperation(value = "Close/Release a Product Milestone")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @PUT
    @Path("/{id}/close-milestone")
    public Response closeMilestone(
            @ApiParam(value = "Product Milestone id", required = true) @PathParam("id") Integer id,
            ProductMilestoneRest productMilestoneRest,
            @Context HttpServletRequest httpServletRequest) throws RestValidationException {

        if (httpServletRequest != null) {
            LoggedInUser loginInUser = authenticationProvider.getLoggedInUser(httpServletRequest);
            productMilestoneProvider.closeMilestone(id, productMilestoneRest, loginInUser.getTokenString());
        } else {
            productMilestoneProvider.closeMilestone(id, productMilestoneRest);
        }

        return Response.ok().build();
    }

    @ApiOperation(value = "Cancel Product Milestone Release process.")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = CONFLICTED_CODE, message = CONFLICTED_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    @Path("/{id}/close-milestone-cancel")
    public Response cancelMilestoneClose(
            @ApiParam(value = "Product Milestone id", required = true) @PathParam("id") Integer id,
            @Context HttpServletRequest httpServletRequest) throws RestValidationException {

        productMilestoneProvider.cancelMilestoneCloseProcess(id);
        return Response.ok().build();
    }

    @ApiOperation(value = "Get the artifacts distributed in this milestone")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = ArtifactPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = ArtifactPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/distributed-artifacts")
    public Response getDistributedArtifacts(@ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
                                      @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
                                      @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
                                      @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q,
                                      @ApiParam(value = "Product milestone id", required = true) @PathParam("id") Integer id) {
        return fromCollection(artifactProvider.queryForCollection(pageIndex, pageSize, sort, q, withDistributedInProductMilestone(id)));
    }

    @ApiOperation(value = "Adds an artifact to the list of distributed artifacts for this product milestone")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @POST
    @Path("/{id}/distributed-artifacts/")
    public Response addDistributedArtifact(
            @ApiParam(value = "Product milestone id", required = true) @PathParam("id") Integer id,
            ArtifactRest artifact) throws RestValidationException {
        if (artifact == null || artifact.getId() == null) {
            throw new EmptyEntityException("No valid artifact included in request to add artifact to product milestone id: " + id);
        }
        productMilestoneProvider.addDistributedArtifact(id, artifact.getId());
        return fromEmpty();
    }

    @ApiOperation(value = "Removes an artifact from the specified product milestone")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @DELETE
    @Path("/{id}/distributed-artifacts/{artifactId}")
    public Response removeDistributedArtifact(
            @ApiParam(value = "Product milestone id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = "Artifact id", required = true) @PathParam("artifactId") Integer artifactId) throws
            RestValidationException {
        productMilestoneProvider.removeDistributedArtifact(id, artifactId);
        return fromEmpty();
    }

    @ApiOperation(value = "Gets the set of builds performed during in a Product Milestone cycle")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/performed-builds")
    public Response getPerformedBuilds(
            @ApiParam(value = "Product Milestone id", required = true) @PathParam("id") Integer id,
            @ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q) {
        return fromCollection(buildRecordProvider.queryForCollection(pageIndex, pageSize, sort, q, withPerformedInMilestone(id)));
    }

    @ApiOperation(value = "Gets the set of builds which produced artifacts distributed/shipped in a Product Milestone")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BuildRecordPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/distributed-builds")
    public Response getDistributedBuilds (
            @ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @ApiParam(value = SORTING_DESCRIPTION) @QueryParam(SORTING_QUERY_PARAM) String sort,
            @ApiParam(value = QUERY_DESCRIPTION, required = false) @QueryParam(QUERY_QUERY_PARAM) String q,
            @ApiParam(value = "Product Milestone id", required = true) @PathParam("id") Integer milestoneId) {
        return fromCollection(buildRecordProvider.getAllBuildRecordsWithArtifactsDistributedInProductMilestone(pageIndex, pageSize, sort, q, milestoneId));
    }

    @ApiOperation(value = "Gets the set of builds which produced artifacts distributed/shipped in a Product Milestone")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = ProductMilestoneReleaseSingleton.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = ProductMilestoneReleaseSingleton.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/{id}/releases/latest")
    public Response getLatestRelease(@PathParam("id") Integer milestoneId) {
        //check if milestone exists
        ProductMilestone productMilestone = milestoneRepository.queryById(milestoneId);
        if (productMilestone == null) {
            //respond with NOT_FOUND if there is no milestone with milestoneId
            return Response.status(Response.Status.NOT_FOUND).entity(new Singleton(null)).build();
        }

        //respond with NO_CONTENT if there are no releases in this milestone
        ProductMilestoneReleaseRest productMilestoneReleaseRest = milestoneReleaseProvider.latestForMilestone(productMilestone);
        if (productMilestoneReleaseRest == null) {
            return Response.status(Response.Status.NO_CONTENT).entity(new Singleton(null)).build();
        }

        return fromSingleton(productMilestoneReleaseRest);
    }
}
