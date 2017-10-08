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
package org.jboss.pnc.messaging;

import org.jboss.pnc.messaging.spi.MessagingRuntimeException;
import org.jboss.pnc.messaging.spi.Message;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.Queue;
import javax.jms.TextMessage;
import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class MessageSender {

    @Inject
    private JMSContext context;

    @Resource(mappedName = "/jms/queue/pncQueue")
    private Queue queue;

    /**
     * @throws MessagingRuntimeException
     */
    public void sendToQueue(Message message) {
        sendToQueue(message.toJson());
    }

    /**
     * @throws MessagingRuntimeException
     */
    public void sendToQueue(String message) {
        sendToQueue(message, Collections.EMPTY_MAP);
    }

    /**
     * @throws MessagingRuntimeException
     */
    public void sendToQueue(String message, Map<String, String> headers) {
        TextMessage textMessage = context.createTextMessage(message);

        headers.forEach((k, v) -> {
            try {
                textMessage.setStringProperty(k, v);
            } catch (JMSException e) {
                throw new MessagingRuntimeException(e);
            }
        });
        try {
            context.createProducer().send(queue, textMessage);
        } catch (JMSRuntimeException e) {
            throw new MessagingRuntimeException(e);
        }
    }

}
