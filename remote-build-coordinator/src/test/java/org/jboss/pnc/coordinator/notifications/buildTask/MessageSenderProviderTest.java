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
package org.jboss.pnc.remotecoordinator.notifications.buildTask;

import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.messaging.spi.Message;
import org.jboss.pnc.messaging.spi.MessageSender;
import org.jboss.pnc.messaging.spi.MessagingConfigurationException;
import org.jboss.pnc.test.cdi.TestInstance;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.enterprise.inject.Instance;

import java.util.Map;
import java.util.Optional;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class MessageSenderProviderTest {

    @Test
    public void shouldGetMessageSender() throws MessagingConfigurationException {
        // given
        Instance<MessageSender> messageSenders = getMessageSenders();
        SystemConfig systemConfig = Mockito.mock(SystemConfig.class);
        Mockito.when(systemConfig.getMessageSenderId()).thenReturn(MessageSender2.class.getName());

        // when
        MessageSenderProvider messageSenderProvider = new MessageSenderProvider(messageSenders, systemConfig);

        // expect
        Optional<MessageSender> selectedMessageSender = messageSenderProvider.getMessageSender();
        Assert.assertTrue(selectedMessageSender.isPresent());
        Assert.assertEquals(MessageSender2.class.getName(), selectedMessageSender.get().getMessageSenderId());
        Assert.assertTrue(((AbstractMessageSender) selectedMessageSender.get()).initialized);
    }

    @Test
    public void shouldNotGetMessageSenderWithInvalidId() {
        // given
        Instance<MessageSender> messageSenders = getMessageSenders();
        SystemConfig systemConfig = Mockito.mock(SystemConfig.class);
        Mockito.when(systemConfig.getMessageSenderId()).thenReturn("invalidId");

        Exception exception = null;
        // when
        try {
            new MessageSenderProvider(messageSenders, systemConfig);
        } catch (MessagingConfigurationException e) {
            exception = e;
        }

        // expect
        Assert.assertTrue(exception instanceof MessagingConfigurationException);
    }

    private Instance<MessageSender> getMessageSenders() {
        MessageSender[] messageSenderBeans = new MessageSender[] { new MessageSender1(), new MessageSender2() };
        return new TestInstance<>(messageSenderBeans);
    }

    class MessageSender1 extends AbstractMessageSender {
        @Override
        public String getMessageSenderId() {
            return MessageSender1.class.getName();
        }
    }

    class MessageSender2 extends AbstractMessageSender {
        @Override
        public String getMessageSenderId() {
            return MessageSender2.class.getName();
        }
    }

    abstract class AbstractMessageSender implements MessageSender {

        boolean initialized = false;

        @Override
        public void init() {
            initialized = true;
        }

        @Override
        public void destroy() {

        }

        @Override
        public void sendToTopic(Message message) {

        }

        @Override
        public void sendToTopic(Message message, Map<String, String> headers) {

        }

        @Override
        public void sendToTopic(String message) {

        }

        @Override
        public void sendToTopic(String message, Map<String, String> headers) {

        }
    }
}