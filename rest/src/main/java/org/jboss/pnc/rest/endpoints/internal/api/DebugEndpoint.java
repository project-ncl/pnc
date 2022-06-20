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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jboss.pnc.rest.configuration.SwaggerConstants;

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
