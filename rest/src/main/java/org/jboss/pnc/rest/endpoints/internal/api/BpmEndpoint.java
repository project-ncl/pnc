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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jboss.pnc.bpm.model.causeway.MilestoneReleaseResultRest;
import org.jboss.pnc.dto.tasks.RepositoryCreationResult;
import org.jboss.pnc.rest.configuration.SwaggerConstants;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;

/**
 * This endpoint is used for starting and interacting with BPM processes.
 *
 * @author Jakub Senko
 */
@Tag(name = SwaggerConstants.TAG_INTERNAL)
@Path("/bpm")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface BpmEndpoint {

    @Operation(summary = "", responses = { @ApiResponse(responseCode = SUCCESS_CODE, description = "Success") })
    @POST
    @Path("/repository-creation/completed")
    @Consumes(MediaType.APPLICATION_JSON)
    void repositoryCreationCompleted(RepositoryCreationResult repositoryCreationResult);

    @Operation(summary = "", responses = { @ApiResponse(responseCode = SUCCESS_CODE, description = "Success") })
    @POST
    @Path("/milestone-release/completed")
    @Consumes(MediaType.APPLICATION_JSON)
    void milestoneReleaseCompleted(MilestoneReleaseResultRest milestoneReleaseResult);

}
