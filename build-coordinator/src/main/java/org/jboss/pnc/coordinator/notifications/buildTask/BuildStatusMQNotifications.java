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
package org.jboss.pnc.coordinator.notifications.buildTask;

import org.jboss.pnc.dto.BuildConfigurationRevisionRef;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.messaging.spi.BuildStatusChanged;
import org.jboss.pnc.messaging.spi.Message;
import org.jboss.pnc.messaging.spi.MessageSender;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
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

    public void observeEvent(@Observes BuildStatusChangedEvent event) {
        logger.debug("Observed new status changed event {}.", event);
        messageSender.ifPresent(ms -> send(ms, event));
        logger.debug("Status changed event processed {}.", event);
    }

    private void send(MessageSender ms, BuildStatusChangedEvent event) {
        if (event.getNewStatus() != null) {

            Message message = BuildStatusChanged.builder()
                    .oldStatus(toStringStatus(event.getOldStatus()))
                    .build(event.getBuild())
                    .buildMe();

            ms.sendToTopic(message, prepareHeaders(event));
        }
    }

    private Map<String, String> prepareHeaders(BuildStatusChangedEvent event) {
        BuildConfigurationRevisionRef buildConfigurationAudited = event.getBuild().getBuildConfigRevision();
        Map<String, String> headers = new HashMap<>();
        headers.put("type", "BuildStateChange");
        headers.put("attribute", "state-change");
        headers.put("name", buildConfigurationAudited.getName());
        headers.put("configurationId", buildConfigurationAudited.getId());
        headers.put("configurationRevision", buildConfigurationAudited.getRev().toString());
        headers.put("oldStatus", toStringStatus(event.getOldStatus()));
        headers.put("newStatus", toStringStatus(event.getNewStatus()));
        return headers;
    }

    private String toStringStatus(BuildStatus status) {

        if (status == null) {
            return "";
        } else {
            return status.toString();
        }
    }
}
