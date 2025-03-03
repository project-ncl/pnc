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

import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.pncmetrics.rest.TimedMetric;
import org.jboss.pnc.processor.annotation.Client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jboss.pnc.rest.configuration.SwaggerConstants;

import java.util.Map;

@Tag(name = "Cache statistics")
@Path("/cache")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Client
public interface CacheEndpoint {

    static final String GET_GENERIC_STATS_DESC = "Get general statistics related to Hibernate.";

    interface MapOfMaps extends Map<String, Map<String, Object>> {
    }

    /**
     * {@value GET_GENERIC_STATS_DESC}
     * 
     * @return
     */
    @Operation(
            summary = GET_GENERIC_STATS_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = MapOfMaps.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/statistics")
    @TimedMetric
    public Response getGenericStats();

    static final String GET_SLC_ENTITIES_STATS_DESC = "Get statistics of all entities in second-level cache.";

    /**
     * {@value GET_SLC_ENTITIES_STATS_DESC}
     * 
     * @return
     */
    @Operation(
            summary = GET_SLC_ENTITIES_STATS_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = MapOfMaps.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/entity-statistics")
    @TimedMetric
    public Response getSecondLevelCacheEntitiesStats();

    static final String GET_SLC_REGIONS_STATS_DESC = "Get statistics of all cache region names in second-level cache.";

    /**
     * {@value GET_SLC_REGIONS_STATS_DESC}
     * 
     * @return
     */
    @Operation(
            summary = GET_SLC_REGIONS_STATS_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = MapOfMaps.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/region-statistics")
    @TimedMetric
    public Response getSecondLevelCacheRegionsStats();

    static final String GET_SLC_COLLECTIONS_STATS_DESC = "Get statistics of all collections in second-level cache.";

    /**
     * {@value GET_SLC_COLLECTIONS_STATS_DESC}
     * 
     * @return
     */
    @Operation(
            summary = GET_SLC_COLLECTIONS_STATS_DESC,
            responses = {
                    @ApiResponse(
                            responseCode = SUCCESS_CODE,
                            description = SUCCESS_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = MapOfMaps.class))),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @GET
    @Path("/collection-statistics")
    @TimedMetric
    public Response getSecondLevelCacheCollectionsStats();

    static final String CLEAR_CACHE_DESC = "Delete all content from second level cache. Needs to be admin";

    /**
     * {@value CLEAR_CACHE_DESC}
     * 
     * @return
     */
    @Operation(
            summary = CLEAR_CACHE_DESC,
            tags = SwaggerConstants.TAG_INTERNAL,
            responses = { @ApiResponse(responseCode = SUCCESS_CODE, description = SUCCESS_DESCRIPTION),
                    @ApiResponse(
                            responseCode = SERVER_ERROR_CODE,
                            description = SERVER_ERROR_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = INVALID_CODE,
                            description = INVALID_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @DELETE
    public Response clearCache();

}
