/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jboss.pnc.dto.response.Banner;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.processor.annotation.Client;
import org.jboss.pnc.rest.configuration.SwaggerConstants;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_UPDATED_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.ENTITY_UPDATED_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

@Tag(name = SwaggerConstants.TAG_INTERNAL)
@Path("/generic-setting")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Client
public interface GenericSettingEndpoint {

    static final String GET_ANNOUNCEMENT_BANNER_DESC = "Get announcement banner";

    /**
     * {@value GET_ANNOUNCEMENT_BANNER_DESC}
     * 
     * @deprecated use {@link PncStatusEndpoint} instead. Can be removed at earliest in the following major version,
     *             i.e. "3.0.0", since it breaks the backwards compatibility.
     *
     * @return
     */
    @Operation(
            summary = GET_ANNOUNCEMENT_BANNER_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = Banner.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("announcement-banner")
    @Deprecated(forRemoval = true, since = "2.7.0") // See Javadoc for further info
    public Banner getAnnouncementBanner();

    static final String BANNER_TEXT = "Announcement Banner text";
    static final String SET_ANNOUNCEMENT_BANNER_DESC = "Set announcement banner. Needs to be admin";

    /**
     * {@value SET_ANNOUNCEMENT_BANNER_DESC}
     * 
     * @deprecated use {@link PncStatusEndpoint} instead.
     * 
     * @param banner {@value BANNER_TEXT}
     */
    @Operation(
            summary = SET_ANNOUNCEMENT_BANNER_DESC,
            responses = { @ApiResponse(responseCode = ENTITY_UPDATED_CODE, description = ENTITY_UPDATED_DESCRIPTION),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @POST
    @Path("announcement-banner")
    @Deprecated(forRemoval = true, since = "2.7.0") // See Javadoc for further info
    public void setAnnouncementBanner(@Parameter(description = BANNER_TEXT, required = true) String banner);

    static final String GET_PNC_VERSION_DESC = "Get PNC System version";

    /**
     * {@value GET_PNC_VERSION_DESC}
     *
     * @deprecated Use {@link VersionEndpoint#getCurrentVersion()} instead. Can be removed at earliest in the following
     *             major version, i.e. "3.0.0", since it breaks the backwards compatibility.
     *
     * @return
     */
    @Operation(
            summary = GET_PNC_VERSION_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = Banner.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("pnc-version")
    @Deprecated(forRemoval = true, since = "2.7.0") // See Javadoc for further details.
    public String getPNCVersion();

    static final String PNC_VERSION = "Current PNC System Version";
    static final String SET_PNC_VERSION_DESC = "Set current PNC System Version. Needs to be admin";

    /**
     * {@value SET_PNC_VERSION_DESC}
     *
     * @deprecated This endpoint gets deprecated without any compensation.
     *
     * @param version {@value PNC_VERSION}
     */
    @Operation(
            summary = SET_PNC_VERSION_DESC,
            responses = { @ApiResponse(responseCode = ENTITY_UPDATED_CODE, description = ENTITY_UPDATED_DESCRIPTION),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @POST
    @Path("pnc-version")
    @Deprecated(forRemoval = true, since = "2.7.0") // See Javadoc for further details.
    public void setPNCVersion(@Parameter(description = PNC_VERSION, required = true) String version);

    static final String IS_IN_MAINTENANCE_MODE_DESC = "Get status of maintenance mode";

    /**
     * {@value IS_IN_MAINTENANCE_MODE_DESC}
     * 
     * @return
     */
    @Operation(
            summary = IS_IN_MAINTENANCE_MODE_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("in-maintenance-mode")
    Boolean isInMaintenanceMode();

    static final String IS_USER_ALLOWED_TO_TRIGGER_BUILDS_DESC = "Provides information whether the current user is allowed to trigger builds (pnc-users-admin are always allowed, other users only if maintenance mode is off)";

    /**
     * {@value IS_USER_ALLOWED_TO_TRIGGER_BUILDS_DESC}
     *
     * @return
     */
    @Operation(
            summary = IS_USER_ALLOWED_TO_TRIGGER_BUILDS_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("is-user-allowed-to-trigger-builds")
    Boolean isCurrentUserAllowedToTriggerBuilds();

    static final String MAINTENANCE_REASON = "Reason to activate Maintenance Mode";
    static final String ACTIVATE_MAINTENANCE_MODE_DESC = "Activate maintenance mode.";

    /**
     * {@value ACTIVATE_MAINTENANCE_MODE_DESC} {@value SwaggerConstants#REQUIRES_ADMIN}
     * 
     * @param reason {@value MAINTENANCE_REASON}
     */
    @Operation(
            summary = "[role:pnc-users-admin] " + ACTIVATE_MAINTENANCE_MODE_DESC,
            responses = { @ApiResponse(responseCode = ENTITY_UPDATED_CODE, description = ENTITY_UPDATED_DESCRIPTION),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @POST
    @Path("activate-maintenance-mode")
    public void activateMaintenanceMode(@Parameter(description = MAINTENANCE_REASON, required = true) String reason);

    static final String DEACTIVATE_MAINTENANCE_MODE_DESC = "Deactivate maintenance mode. needs to be admin";

    /**
     * {@value DEACTIVATE_MAINTENANCE_MODE_DESC} {@value SwaggerConstants#REQUIRES_ADMIN}
     */
    @Operation(
            summary = "[role:pnc-users-admin] " + DEACTIVATE_MAINTENANCE_MODE_DESC,
            responses = { @ApiResponse(responseCode = ENTITY_UPDATED_CODE, description = ENTITY_UPDATED_DESCRIPTION),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @POST
    @Path("deactivate-maintenance-mode")
    public void deactivateMaintenanceMode();
}
