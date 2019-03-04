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
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.provider.BuildConfigurationSetProvider;
import org.jboss.pnc.rest.provider.ProductVersionProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.restmodel.ProductVersionRest;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
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

import java.util.List;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.QUERY_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SORTING_QUERY_PARAM;

@Hidden
@Path("/product-versions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductVersionEndpoint extends AbstractEndpoint<ProductVersion, ProductVersionRest> {

    private ProductVersionProvider productVersionProvider;
    private BuildConfigurationSetProvider buildConfigurationSetProvider;

    public ProductVersionEndpoint() {
    }

    @Inject
    public ProductVersionEndpoint(ProductVersionProvider productVersionProvider,
            BuildConfigurationSetProvider buildConfigurationSetProvider) {
        super(productVersionProvider);
        this.productVersionProvider = productVersionProvider;
        this.buildConfigurationSetProvider = buildConfigurationSetProvider;
    }

    @GET
    public Response getAll(@QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q){
        return super.getAll(pageIndex, pageSize, sort, q);
    }

    @GET
    @Path("/{id}")
    public Response getSpecific(
            @PathParam("id") Integer id) {
        return super.getSpecific(id);
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id,
            ProductVersionRest productVersionRest) throws RestValidationException {
        productVersionProvider.update(id, productVersionRest);
        return Response.ok().build();
    }

    @GET
    @Path("/{id}/build-configuration-sets")
    public Response getBuildConfigurationSets(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q,
            @PathParam("id") Integer id) {
        return Response.ok().entity(buildConfigurationSetProvider.getAllForProductVersion(pageIndex, pageSize, sort,
                q, id)).build();
    }

    @PUT
    @Path("/{id}/build-configuration-sets")
    public Response updateBuildConfigurationSets(@PathParam("id") Integer id,
            List<BuildConfigurationSetRest> buildConfigurationSetRests) throws RestValidationException {
        productVersionProvider.updateBuildConfigurationSets(id, buildConfigurationSetRests);
        return Response.ok().build();
    }

    @POST
    public Response createNewProductVersion(ProductVersionRest productVersionRest, @Context UriInfo uriInfo)
            throws RestValidationException {
        return super.createNew(productVersionRest, uriInfo);
    }

}
