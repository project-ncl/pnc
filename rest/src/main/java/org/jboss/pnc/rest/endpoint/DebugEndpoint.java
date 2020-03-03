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
package org.jboss.pnc.rest.endpoint;

import io.swagger.v3.oas.annotations.Hidden;
import org.jboss.pnc.coordinator.builder.BuildQueue;
import org.jboss.pnc.coordinator.notifications.buildTask.MessageSenderProvider;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.messaging.spi.MessageSender;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 1/25/17 Time: 2:25 PM
 */
@Hidden
@Path("/debug")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DebugEndpoint {

    @Inject
    private BuildQueue buildQueue;

    @Inject
    private MessageSenderProvider messageSenderProvider;

    @Inject
    private Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier;

    @GET
    @Path("/build-queue")
    public Response getBuildQueueInfo() {
        String info = buildQueue.getDebugInfo();
        return Response.ok(info).build();
    }

    /**
     * curl -v -X POST http://localhost:8080/pnc-rest/rest/debug/mq-send-dummy-message curl -v -X POST
     * http://localhost:8080/pnc-rest/rest/debug/mq-send-dummy-message?type=status
     */
    @POST
    @Path("/mq-send-dummy-message")
    public Response sendDummyMessageToQueue(@QueryParam("type") String type) {
        Optional<MessageSender> messageSender = messageSenderProvider.getMessageSender();
        if (!messageSender.isPresent()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("Message sender is not available to inject.")
                    .build();
        } else {
            if (type != null && type.equals("status")) {
                buildStatusChangedEventNotifier.fire(
                        new DefaultBuildStatusChangedEvent(newBuild(), BuildStatus.CANCELLED, BuildStatus.CANCELLED));
            } else {
                messageSender.get().sendToTopic("Test Message.");
            }
            return Response.ok().build();
        }
    }

    public static Build newBuild() {
        return Build.builder()
                .id("1")
                .status(BuildStatus.BUILDING)
                .buildContentId("build-42")
                .temporaryBuild(true)
                .build();
    }

}
