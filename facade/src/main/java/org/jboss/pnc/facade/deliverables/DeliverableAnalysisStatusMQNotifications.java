/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.deliverables;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.messaging.spi.AnalysisStatusMessage;
import org.jboss.pnc.messaging.spi.MessageSender;
import org.jboss.pnc.remotecoordinator.notifications.buildTask.MessageSenderProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public void observeEvent(@ObservesAsync DeliverableAnalysisStatusChangedEvent event) {
        logger.debug("Observed new analysis status changed event {}.", event);
        messageSender.ifPresent(ms -> send(ms, event));
        logger.debug("Analysis status changed event processed {}.", event);
    }

    private void send(MessageSender ms, DeliverableAnalysisStatusChangedEvent event) {
        OperationResult result = event.getResult();
        AnalysisStatusMessage message = new AnalysisStatusMessage(
                event.getOperationId(),
                ATTRIBUTE_NAME,
                event.getMilestoneId(),
                event.getStatus().toString(),
                result == null ? null : result.toString(),
                event.getDeliverablesUrls());
        ms.sendToTopic(message, prepareHeaders(message));
    }

    private Map<String, String> prepareHeaders(AnalysisStatusMessage message) {
        Map<String, String> headers = new HashMap<>();
        headers.put("type", "DeliverableAnalysisStateChange");
        headers.put("attribute", ATTRIBUTE_NAME);
        headers.put("milestoneId", message.getMilestoneId());
        headers.put("status", message.getStatus());
        headers.put("result", message.getResult());
        headers.put("operationId", message.getOperationId());
        return headers;
    }
}
