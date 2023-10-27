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
package org.jboss.pnc.rest.endpoints.internal;

import org.jboss.pnc.coordinator.notifications.buildTask.MessageSenderProvider;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.response.LongResponse;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.messaging.spi.MessageSender;
import org.jboss.pnc.rest.endpoints.internal.api.DebugEndpoint;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.spi.datastore.BuildTaskRepository;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.exception.RemoteRequestException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@ApplicationScoped
public class DebugEndpointImpl implements DebugEndpoint {

    @Inject
    private BuildTaskRepository taskRepository;

    @Inject
    private BuildCoordinator coordinator;

    @Inject
    private MessageSenderProvider messageSenderProvider;

    @Inject
    private Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier;

    @Override
    public String getBuildQueueInfo() {
        return taskRepository.getDebugInfo();
    }

    @Override
    public LongResponse getBuildQueueSize() {
        try {
            return LongResponse.builder().number(coordinator.queueSize()).build();
        } catch (RemoteRequestException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendDummyMessageToQueue(String type) {
        Optional<MessageSender> messageSender = messageSenderProvider.getMessageSender();
        if (!messageSender.isPresent()) {
            throw new ServiceUnavailableException();
        } else {
            if (type != null && type.equals("status")) {
                buildStatusChangedEventNotifier.fire(
                        new DefaultBuildStatusChangedEvent(newBuild(), BuildStatus.CANCELLED, BuildStatus.CANCELLED));
            } else {
                messageSender.get().sendToTopic("Test Message.");
            }
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

    @Override
    public void throwEx() throws Exception {
        RuntimeException nested = new RuntimeException("Root exception.");
        throw new Exception("Test exception.", nested);
    }

    @Override
    public void nocontent() throws Exception {
        // void Results in an empty entity body with a 204 status code.
    }

    @Override
    public Response redirect() throws Exception {
        return Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"debug\"")
                .build();
    }

}
