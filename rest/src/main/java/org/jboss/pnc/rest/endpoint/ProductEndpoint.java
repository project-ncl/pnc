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
import org.jboss.pnc.model.Product;
import org.jboss.pnc.rest.provider.ProductProvider;
import org.jboss.pnc.rest.provider.ProductVersionProvider;
import org.jboss.pnc.rest.restmodel.ProductRest;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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

import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.QUERY_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SORTING_QUERY_PARAM;

@Hidden
@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductEndpoint extends AbstractEndpoint<Product, ProductRest> {

    private ProductVersionProvider productVersionProvider;

    public ProductEndpoint() {
    }

    @Inject
    public ProductEndpoint(ProductProvider productProvider, ProductVersionProvider productVersionProvider) {
        super(productProvider);
        this.productVersionProvider = productVersionProvider;
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
    @Path("/{id}")
    public Response getSpecific(@PathParam("id") Integer id) {
        return super.getSpecific(id);
    }

    @POST
    public Response createNew(ProductRest productRest, @Context UriInfo uriInfo) throws RestValidationException {
        return super.createNew(productRest, uriInfo);
    }

    @PUT
    @Path("/{id}")
    public Response update(
            @PathParam("id") Integer productId,
            @NotNull @Valid ProductRest productRest,
            @Context UriInfo uriInfo) throws RestValidationException {
        return super.update(productId, productRest);
    }

    @GET
    @Path("/{id}/product-versions")
    public Response getProductVersions(
            @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q,
            @PathParam("id") Integer productId) {
        return fromCollection(productVersionProvider.getAllForProduct(pageIndex, pageSize, sort, q, productId));
    }

}
