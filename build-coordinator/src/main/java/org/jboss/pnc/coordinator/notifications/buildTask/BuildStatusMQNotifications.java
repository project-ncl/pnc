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
package org.jboss.pnc.coordinator.notifications.buildTask;

import org.jboss.pnc.messaging.spi.BuildStatusChanged;
import org.jboss.pnc.messaging.spi.Message;
import org.jboss.pnc.messaging.spi.MessageSender;
import org.jboss.pnc.messaging.spi.Status;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Dependent
public class BuildStatusMQNotifications {

    private Logger logger = LoggerFactory.getLogger(BuildStatusMQNotifications.class);

    final Optional<MessageSender> messageSender;

    @Inject
    public BuildStatusMQNotifications(MessageSenderProvider messageSenderProvider) {
        this.messageSender = messageSenderProvider.getMessageSender();
    }

    public void observeEvent(@Observes BuildCoordinationStatusChangedEvent event) {
        logger.debug("Observed new status changed event {}.", event);
        messageSender.ifPresent(ms -> send(ms, event));
        logger.debug("Status changed event processed {}.", event);
    }

    private void send(MessageSender ms, BuildCoordinationStatusChangedEvent event) {
        Status newStatus = toMqStatus(event.getNewStatus());
        if (newStatus != null) {

            Message message = new BuildStatusChanged(
                    toStringStatus(getOldStatus(event.getOldStatus())),
                    event.getBuild()
            );
            ms.sendToTopic(message, prepareHeaders(event));
        }
    }

    private Status getOldStatus(BuildCoordinationStatus oldStatus) {
        Status mqOldStatus;
        if (BuildCoordinationStatus.WAITING_FOR_DEPENDENCIES.equals(oldStatus)) {
            mqOldStatus = Status.ACCEPTED;
        } else if (BuildCoordinationStatus.BUILD_COMPLETED.equals(oldStatus)) {
            mqOldStatus = Status.BUILDING;
        } else {
            mqOldStatus = toMqStatus(oldStatus);
        }
        return mqOldStatus;
    }

    private Map<String, String> prepareHeaders(BuildCoordinationStatusChangedEvent event) {
        Map<String, String> headers = new HashMap<>();
        headers.put("type", "BuildStateChange");
        headers.put("attribute", "state");
        headers.put("name", event.getBuildConfigurationName());
        headers.put("configurationId", event.getBuildConfigurationId().toString());
        headers.put("configurationRevision", event.getBuildConfigurationRevision().toString());
        headers.put("oldStatus", toStringStatus(getOldStatus(event.getOldStatus())));
        headers.put("newStatus", toStringStatus(toMqStatus(event.getNewStatus())));
        return headers;
    }

    private String toStringStatus(Status status) {
        if (status == null) {
            return "";
        } else {
            return status.lowercase();
        }
    }

    /**
     *
     * @return Status or null is status is ignored
     */
    private Status toMqStatus(BuildCoordinationStatus status) {
        switch (status) {
            case NEW:
                return null;
            case ENQUEUED:
                return Status.ACCEPTED;
            case WAITING_FOR_DEPENDENCIES:
                return null;
            case BUILDING:
                return Status.BUILDING;
            case BUILD_COMPLETED:
                return null;
            case DONE:
                return Status.SUCCESS;
            case REJECTED:
                return Status.REJECTED;
            case REJECTED_FAILED_DEPENDENCIES:
                return Status.REJECTED;
            case REJECTED_ALREADY_BUILT:
                return Status.REJECTED;
            case SYSTEM_ERROR:
                return Status.FAILED;
            case DONE_WITH_ERRORS:
                return Status.FAILED;
            case CANCELLED:
                return Status.CANCELED;
        }
        throw new IllegalArgumentException("Invalid status " + status);
    }
}
