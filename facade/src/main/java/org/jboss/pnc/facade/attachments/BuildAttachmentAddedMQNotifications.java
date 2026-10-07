/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.attachments;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

import org.jboss.pnc.messaging.spi.BuildAttachmentAdded;
import org.jboss.pnc.messaging.spi.MessageSender;
import org.jboss.pnc.remotecoordinator.notifications.buildTask.MessageSenderProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Dependent
public class BuildAttachmentAddedMQNotifications {
    private final Optional<MessageSender> messageSender;

    @Inject
    public BuildAttachmentAddedMQNotifications(MessageSenderProvider messageSenderProvider) {
        this.messageSender = messageSenderProvider.getMessageSender();
    }

    public void observeEvent(@ObservesAsync BuildAttachmentAddedEvent event) {
        log.debug("Observed new analysis status changed event {}.", event);
        messageSender.ifPresent(ms -> send(ms, event));
        log.debug("Analysis status changed event processed {}.", event);
    }

    private void send(MessageSender ms, BuildAttachmentAddedEvent event) {
        BuildAttachmentAdded message = BuildAttachmentAdded.builder().newAttachment(event.getNewAttachment()).build();
        ms.sendToTopic(message, prepareHeaders(message));
    }

    private Map<String, String> prepareHeaders(BuildAttachmentAdded message) {
        Map<String, String> headers = new HashMap<>();
        headers.put("type", "BuildAttachmentAdded");
        headers.put("attribute", BuildAttachmentAdded.ATTRIBUTE);
        headers.put("buildId", message.getNewAttachment().getBuild().getId());
        headers.put("attachmentName", message.getNewAttachment().getName());
        headers.put("attachmentType", message.getNewAttachment().getType().toString());
        return headers;
    }
}
