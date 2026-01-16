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

import javax.validation.constraints.NotBlank;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jboss.pnc.api.slsa.dto.provenance.v1.Provenance;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.processor.annotation.Client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

@Tag(name = "Slsa Provenance")
@Path("/slsa/build-provenance/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Client
public interface SlsaProvenanceV1Endpoint {

    static final String A_ID = "ID of the artifact";
    static final String A_DIGEST = "Digest of the artifact";

    static final String GET_BY_ARTIFACT_ID_DESCR = "Generate the build provenance for an artifact identified by id.";
    static final String GET_BY_ARTIFACT_DIGEST_DESCR = "Generate build provenance for an artifact identified by an algorithm-qualified digest (for example: ?sha256=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef).";
    static final String FILTER_SHA256_DESC = "Generate build provenance for an artifact identified by sha256, for example: ?sha256=xxx ";
    static final String FILTER_SHA1_DESC = "Generate build provenance for an artifact identified by sha1, for example: ?sha1=xxx ";
    static final String FILTER_MD5_DESC = "Generate build provenance for an artifact identified by md5, for example: ?md5=xxx ";

    /**
     * {@value GET_BY_ARTIFACT_ID_DESCR}
     *
     * @param id {@value A_ID}
     * @return
     */
    @Operation(
            summary = GET_BY_ARTIFACT_ID_DESCR,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = Provenance.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/artifacts/id/{id}")
    Provenance getFromArtifactId(@Parameter(description = A_ID) @PathParam("id") @NotBlank String id);

    /**
     * {@value GET_BY_ARTIFACT_DIGEST_DESCR}
     *
     * @param digest {@value A_DIGEST}
     * @return
     */
    @Operation(
            summary = GET_BY_ARTIFACT_DIGEST_DESCR,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = Provenance.class))),
                    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/artifacts")
    Provenance getFromArtifactDigest(
            @Parameter(description = FILTER_SHA256_DESC) @QueryParam("sha256") String sha256,
            @Parameter(description = FILTER_MD5_DESC) @QueryParam("md5") String md5,
            @Parameter(description = FILTER_SHA1_DESC) @QueryParam("sha1") String sha1);

}
