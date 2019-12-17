package org.jboss.pnc.rest.endpoint;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.pnc.pncmetrics.rest.TimedMetric;
import org.jboss.pnc.rest.provider.CacheProvider;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Author: Andrea Vibelli, andrea.vibelli@gmail.com
 * Date: 12/16/19
 * Time: 11:20 AM
 */
@Path("/cache")
@Produces(MediaType.APPLICATION_JSON)
public class CacheEndpoint {

    @Inject
    private CacheProvider cacheProvider;

    @Deprecated
    public CacheEndpoint() {
    }

    @Inject
    public CacheEndpoint(CacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    @ApiOperation(value = "Get all statistics from second level cache. Needs to be admin")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION)
    })
    @GET
    @TimedMetric
    public Response getStatistics() {
        return Response.ok(cacheProvider.getStatistics()).build();
    }

    @ApiOperation(value = "Get all statistics from second level cache of a specific entity. Needs to be admin")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION)
    })
    @GET
    @Path("/{entityClass}")
    public Response getStatistics(@ApiParam(value = "Entity className", required = true) @PathParam("entityClass") String entityClassName) {
        try {
            return Response.ok(cacheProvider.getStatistics(Class.forName(entityClassName))).build();
        } catch (ClassNotFoundException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @ApiOperation(value = "Delete all content from second level cache. Needs to be admin")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION)
    })
    @DELETE
    public Response clearCache() {
        cacheProvider.clearAllCache();
        return Response.ok().build();
    }

    @ApiOperation(value = "Delete all content from second level cache of a specific entity. Needs to be admin")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION)
    })
    @DELETE
    @Path("/{entityClass}")
    public Response clearCache(@ApiParam(value = "Entity className", required = true) @PathParam("entityClass") String entityClassName) {
        try {
            cacheProvider.clearCache(Class.forName(entityClassName));
            return Response.ok().build();
        } catch (ClassNotFoundException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

}
