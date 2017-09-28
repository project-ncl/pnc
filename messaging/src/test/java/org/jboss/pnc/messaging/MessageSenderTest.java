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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.pnc.test.util.Wait;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class MessageSenderTest {

    private static Logger logger = LoggerFactory.getLogger(MessageSenderTest.class);

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(MessageSender.class)
                .addClass(PncQueueListener.class)
                .addClass(MessageCollector.class)
                .addClass(ContainerTest.class)
                .addClass(Wait.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    public static final String MESSAGE = "TEST-MESSAGE";

    @Inject
    MessageSender messageSender;

    @Inject
    MessageCollector messageCollector;

    @Test
    public void shouldSendMessageToQueue() throws TimeoutException, InterruptedException {
        messageSender.sendToQueue(MESSAGE);
        Wait.forCondition(() -> isMessageReceived(), 10, ChronoUnit.SECONDS);
    }

    private boolean isMessageReceived() {
        Set<String> receivedMessages = messageCollector.getReceivedMessages();
        logger.debug("ReceivedMessages {}.", receivedMessages);
        return receivedMessages.contains(MESSAGE);
    }

}
