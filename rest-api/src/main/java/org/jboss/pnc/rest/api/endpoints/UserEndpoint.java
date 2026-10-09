/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.api.endpoints;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.User;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.processor.annotation.Client;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.BuildPage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Users")
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Client
public interface UserEndpoint {
    static final String U_ID = "ID of the user";

    static final String GET_CURRENT_USER_DESC = "Gets logged user.";

    /**
     * {@value GET_CURRENT_USER_DESC}
     * 
     * @return
     */
    @Operation(
            summary = GET_CURRENT_USER_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = User.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/current")
    User getCurrentUser();

    static final String LOGIN_REDIRECT_DESC = "Triggers authentication and redirects to the specified path after successful login.";

    /**
     * {@value LOGIN_REDIRECT_DESC}
     *
     * @param redirectPath The path to redirect to after authentication
     * @return HTTP 302 redirect response
     */
    @Operation(
            summary = LOGIN_REDIRECT_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = "302",
                            description = "Redirect to the specified path after successful authentication"),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/login/{redirectPath:.+}")
    Response loginAndRedirect(
            @Parameter(description = "Path to redirect to after login") @PathParam("redirectPath") String redirectPath);

    static final String LOGOUT_REDIRECT_DESC = "Logs out the user and redirects to the specified path.";

    /**
     * {@value LOGOUT_REDIRECT_DESC}
     *
     * @param redirectPath The path to redirect to after logout
     * @return HTTP 302 redirect response
     */
    @Operation(
            summary = LOGOUT_REDIRECT_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = "302",
                            description = "Redirect to the specified path after successful logout"),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/logout/{redirectPath:.+}")
    Response logoutAndRedirect(
            @Parameter(
                    description = "Path to redirect to after logout") @PathParam("redirectPath") String redirectPath);

    static final String GET_BUILDS = "Gets all builds triggered by specific user.";

    /**
     * {@value GET_BUILDS}
     *
     * @param id {@value U_ID}
     * @param pageParameters
     * @param buildsFilter
     * @return
     */
    @Operation(
            summary = GET_BUILDS,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = BuildPage.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/{id}/builds")
    Page<Build> getBuilds(
            @Parameter(description = U_ID) @PathParam("id") String id,
            @Valid @BeanParam PageParameters pageParameters,
            @BeanParam BuildsFilterParameters buildsFilter);

}
