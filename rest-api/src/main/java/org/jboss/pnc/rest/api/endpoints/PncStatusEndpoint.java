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
import org.jboss.pnc.dto.PncStatus;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.processor.annotation.Client;
import org.jboss.pnc.rest.configuration.SwaggerConstants;

import javax.validation.Valid;
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
@Path("/pnc-status")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Client
public interface PncStatusEndpoint {

    static final String SET_PNC_STATUS_SUMMARY = "Set the PNC status.";
    static final String SET_PNC_STATUS_DESC = "There are 3 possible types of requests:\n"
            + "1) Setting the generic banner: Only the field banner is filled.\n"
            + "2) Activating the maintenance mode: All the fields of the DTO are required.\n"
            + "3) Deactivating the maintenance mode: Fields banner and isMaintenanceMode are filled.\n"
            + "Note: banner set to empty string is correct.";
    static final String PNC_STATUS = "PNC status";

    /**
     * There are 3 possible types of requests:<br>
     * 1) Setting the generic banner: Only the field banner is filled<br>
     * 2) Activating the maintenance mode: All the fields of the DTO are required.<br>
     * 3) Deactivating the maintenance mode: Field 'banner' is expected to be non-null (empty is correct though) and
     * 'isMaintenanceMode' is expected to be false.
     *
     * @param genericSettingInfo {@value PNC_STATUS}
     */
    @Operation(
            summary = "[role:pnc-users-admin]: " + SET_PNC_STATUS_SUMMARY,
            description = SET_PNC_STATUS_DESC,
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
    void setPncStatus(@Parameter(description = PNC_STATUS) @Valid PncStatus genericSettingInfo);

    static final String GET_PNC_STATUS = "Get the PNC status information.";

    @Operation(
            summary = GET_PNC_STATUS,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = PncStatus.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    PncStatus getPncStatus();
}
