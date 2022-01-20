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

import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.messaging.spi.Message;
import org.jboss.pnc.messaging.spi.MessageSender;
import org.jboss.pnc.messaging.spi.MessagingRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Singleton
@Slf4j
public class DefaultMessageSender implements MessageSender {

    private Logger logger = LoggerFactory.getLogger(DefaultMessageSender.class);
    private static final int DEFAULT_MESSAGE_SEND_RETRIES = 3;

    @Resource(mappedName = "java:/ConnectionFactory")
    protected ConnectionFactory connectionFactory;

    @Resource(lookup = "java:/jms/queue/pncTopic")
    protected Destination destination;

    protected Connection connection;

    @Override
    public String getMessageSenderId() {
        return DefaultMessageSender.class.getName();
    }

    @Override
    public void init() {
        try {
            connection = connectionFactory.createConnection();
            logger.info("JMS client ID {}.", connection.getClientID());
            ExceptionListener internalExceptionListener = e -> logger.error("JMS exception.", e);
            connection.setExceptionListener(internalExceptionListener);
        } catch (Exception e) {
            throw new MessagingRuntimeException("Failed to initialize JMS.", e);
        }
    }

    @PreDestroy
    public void destroy() {
        closeConnection();
    }

    protected void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                logger.error("Failed to close JMS connection.", e);
            }
        }
    }

    @Override
    public void sendToTopic(Message message) {
        sendToTopic(message.toJson());
    }

    @Override
    public void sendToTopic(Message message, Map<String, String> headers) {
        sendToTopic(message.toJson(), headers);
    }

    @Override
    public void sendToTopic(String message) {
        sendToTopic(message, Collections.EMPTY_MAP);
    }

    @Override
    public void sendToTopic(String message, Map<String, String> headers) {
        doSendMessage(message, headers);
    }

    /**
     * @param message
     * @param headers
     * @throws MessagingRuntimeException
     */
    protected void doSendMessage(String message, Map<String, String> headers) {
        doSendMessagePrivate(message, headers, DEFAULT_MESSAGE_SEND_RETRIES);
    }

    private void doSendMessagePrivate(String message, Map<String, String> headers, int retries) {
        Session session = null;
        MessageProducer messageProducer = null;
        try {
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            messageProducer = session.createProducer(destination);
            sendUsingProducer(message, headers, session, messageProducer);
        } catch (Exception e) {
            if (retries <= 1) {
                // give up
                throw new MessagingRuntimeException(
                        "Cannot send the message: " + message + "; with headers: " + headers + ".",
                        e);
            } else {
                sleep(retries);
                // try to set up a new connection on exception for the next message
                init();
                doSendMessagePrivate(message, headers, retries - 1);
            }
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException e) {
                    logger.error("Cannot close JMS session.");
                }
            }
            if (messageProducer != null) {
                try {
                    messageProducer.close();
                } catch (JMSException e) {
                    logger.error("Cannot close JMS messageProducer.");
                }
            }
        }
    }

    protected void sendUsingProducer(
            String message,
            Map<String, String> headers,
            Session session,
            MessageProducer messageProducer) {
        TextMessage textMessage;
        try {
            textMessage = session.createTextMessage(message);
            textMessage.setStringProperty("producer", "PNC");
        } catch (JMSException e) {
            throw new MessagingRuntimeException(e);
        }
        if (textMessage == null) {
            logger.error("Unable to create textMessage.");
            throw new MessagingRuntimeException("Unable to create textMessage.");
        }

        StringBuilder headerBuilder = new StringBuilder();
        headers.forEach((k, v) -> {
            try {
                textMessage.setStringProperty(k, v);
                headerBuilder.append(k + ":" + v + "; ");
            } catch (JMSException e) {
                throw new MessagingRuntimeException(e);
            }
        });
        try {
            logger.debug("Sending message with headers: {}.", headerBuilder.toString());
            messageProducer.send(textMessage);
        } catch (JMSException e) {
            throw new MessagingRuntimeException(e);
        }
    }

    /**
     * Sleep exponentially as the number of retries decrease, up till max back-off time of 1024 millisecond
     *
     * @param retries
     */
    private static void sleep(int retries) {
        int sleepMilli = (int) Math.min(Math.pow(2, (double) 10 / retries) * 10, 1024);
        waitForNoException(sleepMilli);
    }

    /**
     * Wrapper Thread.sleep function where no exceptions are thrown
     *
     * @param millis time to sleep
     */
    public static void waitForNoException(long millis) {

        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.warn("Sleeping was interrupted", e);
        }
    }
}
