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
package org.jboss.pnc.messaging;

import org.jboss.pnc.messaging.spi.MessageSender;
import org.jboss.pnc.messaging.spi.MessagingRuntimeException;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.pnc.test.util.Wait;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
abstract class BaseMessageSenderTest {

    @Inject
    MessageCollector messageCollector;

    private Logger logger = LoggerFactory.getLogger(BaseMessageSenderTest.class);

    protected static JavaArchive getDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(MessageSender.class)
                .addClass(BaseMessageSenderTest.class)
                .addClass(DefaultMessageSender.class)
                .addClass(PncTopicListener.class)
                .addClass(MessageCollector.class)
                .addClass(ContainerTest.class)
                .addClass(Wait.class)
                .addPackages(true, MessagingRuntimeException.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    protected boolean receivedMessageContains(String message) {
        return receivedMessageContains(message, Collections.EMPTY_MAP);
    }

    protected boolean receivedMessageContains(String message, Map<String, String> headers) {
        Set<TextMessage> receivedMessages = messageCollector.getReceivedMessages();
        logger.debug("ReceivedMessages {}.", receivedMessages);

        Optional<TextMessage> collected = receivedMessages.stream().filter(m -> {
            try {
                return m.getText().equals(message);
            } catch (JMSException e) {
                throw new MessagingRuntimeException(e);
            }
        }).findFirst();

        if (collected.isPresent()) {
            TextMessage textMessage = collected.get();
            try {
                logger.info("Found message {}.", textMessage.getText());
            } catch (JMSException e) {
                logger.error("Cannot read textMessage.", e);
            }
            return containsExpectedHeaders(headers, textMessage);
        }
        return false;
    }

    private boolean containsExpectedHeaders(Map<String, String> headers, TextMessage textMessage) {
        int matchingHeaders = 0;
        for (Map.Entry<String, String> header : headers.entrySet()) {
            Optional<String> optionalHeader = null;
            try {
                optionalHeader = Optional.ofNullable(textMessage.getStringProperty(header.getKey()));
            } catch (JMSException e) {
                logger.error("Cannot read message headers.", e);
                return false;
            }
            if (optionalHeader.isPresent()) {
                if (optionalHeader.get().equals(header.getValue())) {
                    matchingHeaders++;
                }
            } else {
                return false;
            }
        }
        return matchingHeaders == headers.size();
    }

}
