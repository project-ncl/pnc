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
package org.jboss.pnc.rest.api.endpoints;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.processor.annotation.Client;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.ArtifactPage;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;



import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

@Tag(name = "Artifacts")
@Path("/artifacts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Client
public interface ArtifactEndpoint {
    @Operation(summary = "Gets all artifacts.",
            responses = {
                    @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ArtifactPage.class))),
                    @ApiResponse(responseCode = INVALID_CODE, description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @GET
    Page<Artifact> getAll(
            @BeanParam PageParameters pageParams,
            @Parameter(description = "Filter by sha256 of the artifact") @QueryParam("sha256") String sha256,
            @Parameter(description = "Filter by md5 of the artifact") @QueryParam("md5") String md5,
            @Parameter(description = "Filter by sha1 of the artifact") @QueryParam("sha1") String sha1);

    @Operation(summary = "Gets a specific build config.",
            responses = {
                    @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = Artifact.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(responseCode = SERVER_ERROR_CODE, description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @GET
    @Path("/{id}")
    Artifact getSpecific(
            @Parameter(description = "ID of the Artifact") @PathParam("id") int id);
}
