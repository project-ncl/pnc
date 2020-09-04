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
package org.jboss.pnc.coordinator.notifications.buildTask;

import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.messaging.spi.MessageSender;
import org.jboss.pnc.messaging.spi.MessagingConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class MessageSenderProvider {

    private Instance<MessageSender> messageSenders;

    private Optional<MessageSender> messageSender;

    private SystemConfig config;

    private final Logger logger = LoggerFactory.getLogger(MessageSenderProvider.class);

    private static final String DEFAULT_MESSAGE_SENDER = "org.jboss.pnc.messaging.DefaultMessageSender";

    /**
     * Required by CDI
     */
    @Deprecated
    public MessageSenderProvider() {
    }

    @Inject
    public MessageSenderProvider(Instance<MessageSender> messageSenders, SystemConfig config)
            throws MessagingConfigurationException {
        this.messageSenders = messageSenders;
        this.config = config;
        this.messageSender = selectMessageSender();
    }

    public Optional<MessageSender> getMessageSender() {
        return messageSender;
    }

    private Optional<MessageSender> selectMessageSender() throws MessagingConfigurationException {
        Optional<MessageSender> selectedMessageSender;
        if (messageSenders.isUnsatisfied()) {
            logger.warn("Messaging to MQ is disabled. There is no message sender available to inject.");
            selectedMessageSender = Optional.empty();
        } else if (messageSenders.isAmbiguous()) {
            logAvailableInstances(logger, messageSenders);
            MessageSender matchingMessageSender = null;
            String messageSenderName = config.getMessageSenderId();
            if (!StringUtils.isEmpty(messageSenderName)) {
                matchingMessageSender = selectMessageSenderByClassName(messageSenders, messageSenderName);
            }
            if (matchingMessageSender == null) {
                matchingMessageSender = selectMessageSenderByClassName(messageSenders, DEFAULT_MESSAGE_SENDER);
            }
            if (matchingMessageSender != null) {
                logger.info("Using {} MQ message sender.", matchingMessageSender.getMessageSenderId());
                selectedMessageSender = Optional.of(matchingMessageSender);
            } else {
                throw new MessagingConfigurationException(
                        "Non of the available MessageSenders is matching configured name or the default.");
            }
        } else {
            logger.info("Using {} MQ message sender.", messageSenders.get().getClass().getName());
            selectedMessageSender = Optional.of(messageSenders.get());
        }
        selectedMessageSender.ifPresent(MessageSender::init);
        return selectedMessageSender;
    }

    private MessageSender selectMessageSenderByClassName(Instance<MessageSender> messageSenders, String className) {
        for (MessageSender ms : messageSenders) {
            if (ms.getMessageSenderId().equals(className)) {
                return ms;
            }
        }
        return null;
    }

    private void logAvailableInstances(Logger logger, Instance<MessageSender> instances) {
        List<String> names = new ArrayList<>();
        for (MessageSender instance : instances) {
            names.add(instance.getMessageSenderId());
        }
        logger.info("Found multiple message senders: {}.", names.stream().collect(Collectors.joining(", ")));
    }

}
