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


import org.jboss.pnc.dto.GroupConfig;
import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.GroupConfigPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.ProductVersionPage;
import org.jboss.pnc.rest.api.swagger.response.SwaggerSingletons.ProductVersionSingleton;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.List;

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

import javax.ws.rs.BeanParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "")
@Path("/product-versions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ProductVersionEndpoint{

    @Operation(summary = "Gets all Product Versions",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ProductVersionPage.class))),
                @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ProductVersionPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    public Response getAll(@BeanParam PageParameters pageParameters);

    @Operation(summary = "Gets specific Product Version",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ProductVersionSingleton.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}")
    public Response getSpecific(
            @Parameter(description = "Product Version id", required = true) @PathParam("id") Integer id);

    @Operation(summary = "Updates an existing Product Version",
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
    public Response update(@Parameter(description = "Product Version id", required = true) @PathParam("id") Integer id,
            ProductVersion productVersion);

    @Operation(summary = "Gets build configuration sets associated with a product version",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = GroupConfigPage.class))),
                @ApiResponse(responseCode = NO_CONTENT_CODE, description = NO_CONTENT_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = GroupConfigPage.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GET
    @Path("/{id}/build-configuration-sets")
    public Response getGroupConfigs(
            @BeanParam PageParameters pageParameters,
            @Parameter(description = "Product Version id", required = true) @PathParam("id") Integer id);

    @PUT
    @Path("/{id}/build-configuration-sets")
    public Response updateGroupConfigs(@Parameter(description = "Product Version id", required = true) @PathParam("id") Integer id,
            List<GroupConfig> buildConfigurationSets);

    @Operation(summary = "Create a new ProductVersion for a Product",
            responses = {
                @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ProductVersionSingleton.class))),
                @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = CONFLICTED_CODE, description = CONFLICTED_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @POST
    public Response createNewProductVersion(ProductVersion productVersion);

}
