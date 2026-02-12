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
import org.jboss.pnc.dto.internal.BuildResultRest;
import org.jboss.pnc.processor.annotation.Client;
import org.jboss.pnc.rest.configuration.SwaggerConstants;
import org.jboss.pnc.rex.model.requests.NotificationRequest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

@Tag(name = SwaggerConstants.TAG_INTERNAL)
@Path("/build-tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Client
public interface BuildTaskEndpoint {

    @Operation(
            summary = "Notifies the completion of externally managed build task process.",
            responses = { @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION) })
    @POST
    @Path("/{taskId}/completed")
    @Consumes(MediaType.APPLICATION_JSON)
    @Deprecated
    Response buildTaskCompleted(
            @Parameter(description = "Build task id") @PathParam("taskId") String buildId,
            @Parameter(description = "Build result", required = true) BuildResultRest buildResult);

    @Operation(
            summary = "Persists build or notifies client about a status transition in a active build process.",
            responses = { @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION) })
    @POST
    @Path("/{buildId}/notify")
    @Consumes(MediaType.APPLICATION_JSON)
    Response buildTaskNotification(
            @Parameter(description = "Build id") @PathParam("buildId") @NotBlank String buildId,
            @Parameter(description = "Notification body", required = true) @NotNull NotificationRequest notification);
}
