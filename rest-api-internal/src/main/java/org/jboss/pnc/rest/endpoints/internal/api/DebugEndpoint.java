/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.endpoints.internal.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.pnc.dto.response.LongResponse;
import org.jboss.pnc.rest.configuration.SwaggerConstants;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 1/25/17 Time: 2:25 PM
 */
@Hidden
@Tag(name = SwaggerConstants.TAG_INTERNAL)
@Path("/debug")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DebugEndpoint {

    @GET
    @Path("/build-queue")
    String getBuildQueueInfo();

    @GET
    @Path("/build-queue/size")
    LongResponse getBuildQueueSize();

    /**
     * curl -v -X POST http://localhost:8080/pnc-rest/v2/debug/mq-send-dummy-message curl -v -X POST
     * http://localhost:8080/pnc-rest/v2/debug/mq-send-dummy-message?type=status
     */
    @POST
    @Path("/mq-send-dummy-message")
    void sendDummyMessageToQueue(@QueryParam("type") String type);

    @GET
    @Path("/throw")
    public void throwEx() throws Exception;

    @GET
    @Path("/nocontent")
    public void nocontent() throws Exception;

    @GET
    @Path("/unauthorized")
    public Response redirect() throws Exception;

}
