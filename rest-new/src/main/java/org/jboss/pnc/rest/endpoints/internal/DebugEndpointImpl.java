/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.coordinator.builder.BuildQueue;
import org.jboss.pnc.coordinator.notifications.buildTask.MessageSenderProvider;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.messaging.spi.MessageSender;
import org.jboss.pnc.rest.endpoints.internal.api.DebugEndpoint;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.ServiceUnavailableException;
import java.util.Optional;

/**
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@ApplicationScoped
public class DebugEndpointImpl implements DebugEndpoint {

    @Inject
    private BuildQueue buildQueue;

    @Inject
    private MessageSenderProvider messageSenderProvider;

    @Inject
    private Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier;

    @Override
    public String getBuildQueueInfo() {
        return buildQueue.getDebugInfo();
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
}
