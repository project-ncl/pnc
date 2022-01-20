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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.messaging.spi.MessageSender;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.pnc.test.util.Wait;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class MessageSenderTest extends BaseMessageSenderTest {

    private static Logger logger = LoggerFactory.getLogger(MessageSenderTest.class);

    @Deployment
    public static Archive<?> deployment() {
        return getDeployment();
    }

    @Inject
    MessageSender messageSender;

    @Before
    public void init() {
        messageSender.init();
    }

    @Test
    public void shouldSendMessage() throws InterruptedException {
        String message = "TEST-MESSAGE";
        messageSender.sendToTopic(message);
        try {
            Wait.forCondition(() -> receivedMessageContains(message), 10, ChronoUnit.SECONDS);
        } catch (TimeoutException e) {
            Assert.fail("Did not received expected massage.");
        }
    }

    @Test
    public void shouldSendMessageWithHeaders() throws InterruptedException {
        String message = "TEST MESSAGE WITH HEADERS.";

        Map<String, String> headers = new HashMap<>();
        headers.put("key", "value");
        headers.put("id", "12345");

        messageSender.sendToTopic(message, headers);
        try {
            Wait.forCondition(() -> receivedMessageContains(message, headers), 10, ChronoUnit.SECONDS);
        } catch (TimeoutException e) {
            Assert.fail("Did not received expected massage.");
        }
    }
}
