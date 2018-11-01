/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.api.endpoints;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.CONFLICTED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NO_CONTENT_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NO_CONTENT_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

import org.jboss.pnc.dto.ProductRelease;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.ProductReleasePage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerSingletons.ProductReleaseSingleton;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;


@Tag(name = "")
@Path("/product-releases")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ProductReleaseEndpoint{

    @Operation(summary = "Gets all Product Releases",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ProductReleasePage.class))),
                @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ProductReleasePage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    public Response getAll(@BeanParam PageParameters pageParameters);

    @Operation(summary = "Gets all Product Releases of the Specified Product Version",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ProductReleasePage.class))),
                @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ProductReleasePage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/product-versions/{versionId}")
    public Response getAllByProductVersionId(
            @BeanParam PageParameters pageParameters,
            @Parameter(description = "Product Version id", required = true) @PathParam("versionId") Integer versionId);

    @Operation(summary = "Gets specific Product Release",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ProductReleaseSingleton.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ProductReleaseSingleton.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}")
    public Response getSpecific(@Parameter(description = "Product Release id", required = true) @PathParam("id") Integer id);

    @Operation(summary = "Creates a new Product Release",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ProductReleaseSingleton.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = CONFLICTED_CODE, description = CONFLICTED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    public Response createNew(ProductRelease productRelease);

    @Operation(summary = "Updates an existing Product Release",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = CONFLICTED_CODE, description = CONFLICTED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PUT
    @Path("/{id}")
    public Response update(@Parameter(description = "Product Release id", required = true) @PathParam("id") Integer id,
            ProductRelease productRelease);

    @Operation(summary = "Gets all Product Releases Support Level",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = SupportLevelPage.class))),
                @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = SupportLevelPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/support-level")
    public Response getAllSupportLevel();

    @Operation(summary = "Gets all Builds distributed for Product Version",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildIds.class))),
                @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = BuildIds.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/distributed-build-records-ids")
    public Response getAllBuildsInDistributedRecordsetOfProductRelease(
            @BeanParam PageParameters pageParametersuery,
            @Parameter(description = "Product Release id", required = true) @PathParam("id") Integer id);

}
