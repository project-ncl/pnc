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

package org.jboss.pnc.rest.endpoints.internal.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jboss.pnc.bpm.model.BuildExecutionConfigurationRest;
import org.jboss.pnc.bpm.model.BuildExecutionConfigurationWithCallbackRest;
import org.jboss.pnc.bpm.model.BuildResultRest;
import org.jboss.pnc.facade.validation.InvalidEntityException;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.FORBIDDEN_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.FORBIDDEN_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

@Tag(name = "Internal")
@Path("/build-tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface BuildTaskEndpoint {

    @Operation(
            summary = "DEPRECATED: use single json instead of form parameters.",
            responses = { @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION) })
    @POST
    @Path("/{taskId}/completed")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Deprecated
    public Response buildTaskCompleted(
            @Parameter(description = "Build task id") @PathParam("taskId") String buildId,
            @Parameter(
                    description = "Build result",
                    required = true) @FormParam("buildResult") BuildResultRest buildResult)
            throws InvalidEntityException;

    @Operation(
            summary = "Notifies the completion of externally managed build task process.",
            responses = { @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION) })
    @POST
    @Path("/{taskId}/completed")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response buildTaskCompletedJson(
            @Parameter(description = "Build task id") @PathParam("taskId") String buildId,
            @Parameter(description = "Build result", required = true) BuildResultRest buildResult)
            throws InvalidEntityException;

    @Operation(
            summary = "DEPRECATED: Triggers the build execution for a given configuration.",
            responses = { @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION),
                    @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION),
                    @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION),
                    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_DESCRIPTION), })
    @POST
    @Path("/execute-build")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Deprecated
    public Response build(
            @Parameter(
                    description = "Build Execution Configuration. See org.jboss.pnc.spi.executor.BuildExecutionConfiguration.",
                    required = true) @FormParam("buildExecutionConfiguration") BuildExecutionConfigurationRest buildExecutionConfiguration,
            @Parameter(
                    description = "Username who triggered the build. If empty current user is used.") @FormParam("usernameTriggered") String usernameTriggered,
            @Parameter(description = "Optional Callback URL") @FormParam("callbackUrl") String callbackUrl);

    @Operation(
            summary = "Triggers the build execution for a given configuration.",
            responses = { @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION),
                    @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION),
                    @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION),
                    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_DESCRIPTION), })
    @POST
    @Path("/execute-build")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response build(
            @Parameter(
                    description = "Build Execution Configuration. See org.jboss.pnc.spi.executor.BuildExecutionConfiguration.",
                    required = true) BuildExecutionConfigurationWithCallbackRest buildExecutionConfiguration);

    @Operation(
            summary = "Cancel the build execution defined with given executionConfigurationId.",
            responses = { @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION),
                    @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION),
                    @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION),
                    @ApiResponse(responseCode = FORBIDDEN_CODE, description = FORBIDDEN_DESCRIPTION), })
    @POST
    @Path("/cancel-build/{buildExecutionConfigurationId}")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED }) // TODO REMOVE
                                                                                     // APPLICATION_FORM_URLENCODED
    public Response cancelBuild(
            @Parameter(
                    description = "Build Execution Configuration ID. See org.jboss.pnc.spi.executor.BuildExecutionConfiguration.") @PathParam("buildExecutionConfigurationId") String buildExecutionConfigurationId);

}
