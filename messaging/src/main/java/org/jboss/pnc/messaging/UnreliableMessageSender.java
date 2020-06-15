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
package org.jboss.pnc.messaging;

import org.jboss.pnc.common.concurrent.MDCThreadPoolExecutor;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.messaging.spi.MessageSender;
import org.jboss.pnc.messaging.spi.MessagingRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;

/**
 * Message sender does not guarantee message delivery to MQ. Unsent messages are logged as errors.
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Singleton
public class UnreliableMessageSender extends DefaultMessageSender implements MessageSender {

    private Logger logger = LoggerFactory.getLogger(UnreliableMessageSender.class);

    private BlockingQueue<Runnable> workQueue;

    private ExecutorService executor;

    private int workQueueSize;

    public UnreliableMessageSender() {
        workQueueSize = 1000;
    }

    public UnreliableMessageSender(int workQueueSize) {
        this.workQueueSize = workQueueSize;
    }

    @Inject
    public UnreliableMessageSender(SystemConfig systemConfig) {
        workQueueSize = systemConfig.getMessagingInternalQueueSize();
    }

    @Override
    public String getMessageSenderId() {
        return UnreliableMessageSender.class.getName();
    }

    @Override
    public void init() {
        workQueue = new ArrayBlockingQueue<>(workQueueSize);
        RejectedExecutionHandler handler = (r, executor) -> {
            logUnsent(r);
        };
        executor = new MDCThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, workQueue, handler);

        executor.execute(() -> {
            super.init();
        });
    }

    @Override
    @PreDestroy
    public void destroy() {
        logger.info("Destroying JMS sender.");
        logger.debug("There are {} messages in queue.", workQueue.size());
        List<Runnable> unSentMessages = executor.shutdownNow();
        unSentMessages.forEach(r -> logUnsent(r));

        List<Runnable> messagesInQueue = new ArrayList<>();
        workQueue.drainTo(messagesInQueue);
        messagesInQueue.forEach(r -> logUnsent(r));

        closeConnection();
        logger.info("JMS sender destroyed.");
    }

    private void logUnsent(Runnable r) {
        if (r instanceof SendTask) {
            SendTask sendTask = (SendTask) r;
            logger.error("Unable to send JMS message. Message: {}, Headers: {}.", sendTask.message, sendTask.headers);
        } else {
            logger.error("There is a non-completed JMS task (probably a connection attempt).");
        }
    }

    @Override
    public void sendToTopic(String message, Map<String, String> headers) {
        logger.trace("There are {} messages in queue.", workQueue.size());
        executor.execute(new SendTask(message, headers));
    }

    private class SendTask implements Runnable {

        private final String message;

        private final Map<String, String> headers;

        public SendTask(String message, Map<String, String> headers) {
            this.message = message;
            this.headers = headers;
        }

        @Override
        public void run() {
            logger.debug("Sending a JMS message: {}, with headers: {}.", message, headers);
            try {
                doSendMessage(message, headers);
            } catch (MessagingRuntimeException e) {
                logger.error("Cannot send the message: " + message + "; with headers: " + headers + ".", e);
            }
        }
    }
}
