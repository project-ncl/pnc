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

package org.jboss.pnc.facade.deliverables;

import org.jboss.pnc.coordinator.notifications.buildTask.MessageSenderProvider;
import org.jboss.pnc.messaging.spi.AnalysisStatusMessage;
import org.jboss.pnc.messaging.spi.Message;
import org.jboss.pnc.messaging.spi.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author jakubvanko
 */
@Dependent
public class DeliverableAnalysisStatusMQNotifications {

    private final String ATTRIBUTE_NAME = "deliverable-analysis-state-change";

    private final Logger logger = LoggerFactory.getLogger(DeliverableAnalysisStatusMQNotifications.class);
    private final Optional<MessageSender> messageSender;

    @Inject
    public DeliverableAnalysisStatusMQNotifications(MessageSenderProvider messageSenderProvider) {
        this.messageSender = messageSenderProvider.getMessageSender();
    }

    public void observeEvent(@Observes DeliverableAnalysisStatusChangedEvent event) {
        logger.debug("Observed new analysis status changed event {}.", event);
        messageSender.ifPresent(ms -> send(ms, event));
        logger.debug("Analysis status changed event processed {}.", event);
    }

    private void send(MessageSender ms, DeliverableAnalysisStatusChangedEvent event) {
        Message message = new AnalysisStatusMessage(
                ATTRIBUTE_NAME,
                event.getMilestoneId(),
                event.getStatus().toString(),
                event.getDeliverablesUrls());
        ms.sendToTopic(message, prepareHeaders(event));
    }

    private Map<String, String> prepareHeaders(DeliverableAnalysisStatusChangedEvent event) {
        Map<String, String> headers = new HashMap<>();
        headers.put("type", "DeliverableAnalysisStateChange");
        headers.put("attribute", ATTRIBUTE_NAME);
        headers.put("milestoneId", event.getMilestoneId().orElse("null"));
        headers.put("status", event.getStatus().toString());
        return headers;
    }
}
