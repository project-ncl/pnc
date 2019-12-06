/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.jboss.pnc.rest.provider.GenericSettingProvider;
import org.jboss.pnc.rest.restmodel.BannerRest;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

@Api(value = "/generic-setting", description = "Generic Global settings")
@Path("/generic-setting")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GenericSettingEndpoint {

    @Inject
    private GenericSettingProvider genericSettingProvider;

    @ApiOperation(value = "Get announcement banner")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BannerRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION)
    })
    @GET
    @Path("announcement-banner")
    public Response getAnnouncementBanner() {

        BannerRest banner = new BannerRest();
        banner.setBanner(genericSettingProvider.getAnnouncementBanner());
        return Response.ok(banner).build();
    }

    @ApiOperation(value = "Set announcement banner")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION)
    })
    @POST
    @Path("announcement-banner")
    public Response setAnnouncementBanner(BannerRest banner) {
        genericSettingProvider.setAnnouncementBanner(banner.getBanner());
        return Response.ok().build();
    }

    @ApiOperation(value = "Get status of maintenance mode")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = Boolean.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION)
    })
    @GET
    @Path("in-maintenance-mode")
    public Response isInMaintenanceMode() {
        return Response.ok(genericSettingProvider.isInMaintenanceMode()).build();
    }

    @ApiOperation(value = "Activate maintenance mode. Needs to be admin")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION)
    })
    @POST
    @Path("activate-maintenance-mode")
    public Response activateMaintenanceMode(String reason) {
        genericSettingProvider.activateMaintenanceMode(reason);
        return Response.ok().build();
    }

    @ApiOperation(value = "Deactivate maintenance mode. Needs to be admin")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION)
    })
    @POST
    @Path("deactivate-maintenance-mode")
    public Response deactivateMaintenanceMode() {
        genericSettingProvider.deactivateMaintenanceMode();
        return Response.ok().build();
    }
}
