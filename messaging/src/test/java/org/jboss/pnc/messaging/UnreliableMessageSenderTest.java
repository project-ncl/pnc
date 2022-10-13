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
import org.jboss.pnc.common.concurrent.MDCThreadPoolExecutor;
import org.jboss.pnc.common.concurrent.MDCWrappers;
import org.jboss.pnc.common.json.AbstractModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.KeycloakClientConfig;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
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

import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextStorage;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class UnreliableMessageSenderTest extends BaseMessageSenderTest {

    private static Logger logger = LoggerFactory.getLogger(UnreliableMessageSenderTest.class);

    @Deployment
    public static Archive<?> deployment() {
        return getDeployment().addClass(UnreliableMessageSender.class)
                .addClass(SystemConfig.class)
                .addClass(AbstractModuleConfig.class)
                .addClass(KeycloakClientConfig.class)
                .addClass(SysConfigProducer.class)
                .addClass(Context.class)
                .addClass(ContextStorage.class)
                .addClass(MDCThreadPoolExecutor.class)
                .addClass(MDCWrappers.class);
    }

    @Inject
    Instance<MessageSender> messageSenders;

    MessageSender messageSender;

    @Before
    public void init() {
        for (MessageSender sender : messageSenders) {
            if (sender.getMessageSenderId().equals(UnreliableMessageSender.class.getName())) {
                messageSender = sender;
            }
        }
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

}
