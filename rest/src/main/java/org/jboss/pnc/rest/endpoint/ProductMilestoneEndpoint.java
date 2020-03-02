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
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.rest.provider.ArtifactProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.provider.ProductMilestoneProvider;
import org.jboss.pnc.rest.provider.ProductMilestoneReleaseProvider;
import org.jboss.pnc.rest.restmodel.ArtifactRest;
import org.jboss.pnc.rest.restmodel.ProductMilestoneReleaseRest;
import org.jboss.pnc.rest.restmodel.ProductMilestoneRest;
import org.jboss.pnc.rest.restmodel.response.Singleton;
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

import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.QUERY_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SORTING_QUERY_PARAM;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withDistributedInProductMilestone;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withPerformedInMilestone;

@Hidden
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

    @GET
    public Response getAll(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q) {
        return super.getAll(pageIndex, pageSize, sort, q);
    }

    @GET
    @Path("/product-versions/{versionId}")
    public Response getAllByProductVersionId(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q,
            @PathParam("versionId") Integer versionId) {
        return fromCollection(
                productMilestoneProvider.getAllForProductVersion(pageIndex, pageSize, sort, q, versionId));
    }

    @GET
    @Path("/{id}")
    public Response getSpecific(@PathParam("id") Integer id) {
        return super.getSpecific(id);
    }

    @POST
    public Response createNew(ProductMilestoneRest productMilestoneRest, @Context UriInfo uriInfo)
            throws RestValidationException {
        return super.createNew(productMilestoneRest, uriInfo);
    }

    @PUT
    @Path("/{id}")
    public Response update(
            @PathParam("id") Integer id,
            ProductMilestoneRest productMilestoneRest,
            @Context UriInfo uriInfo,
            @Context HttpServletRequest httpServletRequest) throws RestValidationException {

        LoggedInUser loginInUser = authenticationProvider.getLoggedInUser(httpServletRequest);

        productMilestoneProvider.update(id, productMilestoneRest, loginInUser.getTokenString());
        return Response.ok().build();
    }

    @PUT
    @Path("/{id}/close-milestone")
    public Response closeMilestone(
            @PathParam("id") Integer id,
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

    @POST
    @Path("/{id}/close-milestone-cancel")
    public Response cancelMilestoneClose(@PathParam("id") Integer id, @Context HttpServletRequest httpServletRequest)
            throws RestValidationException {

        productMilestoneProvider.cancelMilestoneCloseProcess(id);
        return Response.ok().build();
    }

    @GET
    @Path("/{id}/distributed-artifacts")
    public Response getDistributedArtifacts(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q,
            @PathParam("id") Integer id) {
        return fromCollection(
                artifactProvider
                        .queryForCollection(pageIndex, pageSize, sort, q, withDistributedInProductMilestone(id)));
    }

    @POST
    @Path("/{id}/distributed-artifacts/")
    public Response addDistributedArtifact(@PathParam("id") Integer id, ArtifactRest artifact)
            throws RestValidationException {
        if (artifact == null || artifact.getId() == null) {
            throw new EmptyEntityException(
                    "No valid artifact included in request to add artifact to product milestone id: " + id);
        }
        productMilestoneProvider.addDistributedArtifact(id, artifact.getId());
        return fromEmpty();
    }

    @DELETE
    @Path("/{id}/distributed-artifacts/{artifactId}")
    public Response removeDistributedArtifact(@PathParam("id") Integer id, @PathParam("artifactId") Integer artifactId)
            throws RestValidationException {
        productMilestoneProvider.removeDistributedArtifact(id, artifactId);
        return fromEmpty();
    }

    @GET
    @Path("/{id}/performed-builds")
    public Response getPerformedBuilds(
            @PathParam("id") Integer id,
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q) {
        return fromCollection(
                buildRecordProvider.queryForCollection(pageIndex, pageSize, sort, q, withPerformedInMilestone(id)));
    }

    @GET
    @Path("/{id}/distributed-builds")
    public Response getDistributedBuilds(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q,
            @PathParam("id") Integer milestoneId) {
        return fromCollection(
                buildRecordProvider.getAllBuildRecordsWithArtifactsDistributedInProductMilestone(
                        pageIndex,
                        pageSize,
                        sort,
                        q,
                        milestoneId));
    }

    @GET
    @Path("/{id}/releases/latest")
    public Response getLatestRelease(@PathParam("id") Integer milestoneId) {
        // check if milestone exists
        ProductMilestone productMilestone = milestoneRepository.queryById(milestoneId);
        if (productMilestone == null) {
            // respond with NOT_FOUND if there is no milestone with milestoneId
            return Response.status(Response.Status.NOT_FOUND).entity(new Singleton(null)).build();
        }

        // respond with NO_CONTENT if there are no releases in this milestone
        ProductMilestoneReleaseRest productMilestoneReleaseRest = milestoneReleaseProvider
                .latestForMilestone(productMilestone);
        if (productMilestoneReleaseRest == null) {
            return Response.status(Response.Status.NO_CONTENT).entity(new Singleton(null)).build();
        }

        return fromSingleton(productMilestoneReleaseRest);
    }
}
