/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.notifications.websockets;

import org.jboss.pnc.spi.notifications.AttachedClient;
import org.jboss.pnc.spi.notifications.Notifier;
import org.junit.Test;
import org.mockito.internal.verification.Times;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DefaultNotifierTest {

    @Test
    public void shouldAddNotifier() throws Exception {
        //given
        Notifier notifier = new DefaultNotifier();
        AttachedClient attachedClient = mock(AttachedClient.class);

        //when
        notifier.attachClient(attachedClient);

        //then
        assertThat(notifier.getAttachedClientsCount()).isEqualTo(1);
    }

    @Test
    public void shouldCleanItself() throws Exception {
        //given
        DefaultNotifier notifier = new DefaultNotifier();
        AttachedClient attachedClient = mock(AttachedClient.class);
        doReturn(false).when(attachedClient).isEnabled();

        notifier.attachClient(attachedClient);

        //when
        notifier.cleanUp();

        //then
        assertThat(notifier.getAttachedClientsCount()).isEqualTo(0);
    }

    @Test
    public void shouldSendAMessage() throws Exception {
        //given
        Object messageBody = new Object();

        Notifier notifier = new DefaultNotifier();
        AttachedClient attachedClient = mock(AttachedClient.class);
        doReturn(true).when(attachedClient).isEnabled();
        notifier.attachClient(attachedClient);

        //when
        notifier.sendMessage(messageBody);

        //then
        verify(attachedClient).sendMessage(messageBody);
    }

    @Test
    public void shouldNotSendAMessageToDisabledClient() throws Exception {
        //given
        Object messageBody = new Object();

        Notifier notifier = new DefaultNotifier();
        AttachedClient attachedClient = mock(AttachedClient.class);
        doReturn(false).when(attachedClient).isEnabled();
        notifier.attachClient(attachedClient);

        //when
        notifier.sendMessage(messageBody);

        //then
        verify(attachedClient, new Times(0)).sendMessage(messageBody);
    }

    @Test
    public void shouldRemoveAttachedClientWhenItThrowsAnException() throws Exception {
        //given
        Notifier notifier = new DefaultNotifier();
        AttachedClient attachedClient = mock(AttachedClient.class);
        doReturn(true).when(attachedClient).isEnabled();
        doThrow(new Exception("expected")).when(attachedClient).sendMessage(anyObject());
        notifier.attachClient(attachedClient);

        //when
        notifier.sendMessage(new Object());

        //then
        assertThat(notifier.getAttachedClientsCount()).isEqualTo(0);
    }

}