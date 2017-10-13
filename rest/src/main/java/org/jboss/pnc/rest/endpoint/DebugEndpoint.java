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

import org.jboss.pnc.coordinator.builder.BuildQueue;
import org.jboss.pnc.messaging.MessageSender;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 1/25/17
 * Time: 2:25 PM
 */
@Path("/debug")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DebugEndpoint {

    @Inject
    private BuildQueue buildQueue;

    @Inject
    private MessageSender messageSender;

    @GET
    @Path("/build-queue")
    public Response getBuildQueueInfo() {
        String info = buildQueue.getDebugInfo();
        return Response.ok(info).build();
    }

    @POST
    @Path("/mq-send-dummy-message")
    public Response sendDummyMessageToQueue() {
        messageSender.sendToQueue("Test Message.");
        return Response.ok().build();
    }
}
