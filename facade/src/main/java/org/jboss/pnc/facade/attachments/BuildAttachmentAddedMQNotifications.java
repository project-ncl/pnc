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
package org.jboss.pnc.facade.attachments;

import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.messaging.spi.BuildAttachmentAdded;
import org.jboss.pnc.messaging.spi.MessageSender;
import org.jboss.pnc.remotecoordinator.notifications.buildTask.MessageSenderProvider;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
