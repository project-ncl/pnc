/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.jboss.pnc.spi.notifications.AttachedClient;
import org.jboss.pnc.spi.notifications.MessageCallback;
import org.jboss.pnc.spi.notifications.Notifier;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.verification.Times;

public class LocalNotifierTest {

    @Test
    public void shouldAddNotifier() throws Exception {
        // given
        Notifier notifier = new LocalNotifier();
        AttachedClient attachedClient = mock(AttachedClient.class);

        // when
        notifier.attachClient(attachedClient);

        // then
        assertThat(notifier.getAttachedClientsCount()).isEqualTo(1);
    }

    @Test
    public void shouldCleanItself() throws Exception {
        // given
        LocalNotifier notifier = new LocalNotifier();
        AttachedClient attachedClient = mock(AttachedClient.class);
        doReturn(false).when(attachedClient).isEnabled();

        notifier.attachClient(attachedClient);

        // when
        notifier.cleanUp();

        // then
        assertThat(notifier.getAttachedClientsCount()).isEqualTo(0);
    }

    @Test
    public void shouldSendAMessage() throws Exception {
        // given
        Object messageBody = new Object();

        Notifier notifier = new LocalNotifier();
        AttachedClient attachedClient = mock(AttachedClient.class);
        doReturn(true).when(attachedClient).isEnabled();
        notifier.attachClient(attachedClient);

        // when
        notifier.sendMessage(messageBody);

        // then
        verify(attachedClient).sendMessage(messageBody, notifier.getCallback());
        assertThat(notifier.getAttachedClientsCount()).isEqualTo(1);
    }

    @Test
    public void shouldSendAsynchAMessage() throws Exception {

        ArgumentCaptor<MessageCallback> messageCallback = ArgumentCaptor.forClass(MessageCallback.class);

        // given
        Notifier notifier = new LocalNotifier();

        AttachedClient attachedClient = mock(AttachedClient.class);
        doReturn(true).when(attachedClient).isEnabled();

        notifier.attachClient(attachedClient);

        // when
        notifier.sendMessage(new Object());

        // then
        verify(attachedClient).sendMessage(any(), messageCallback.capture());

        messageCallback.getValue().successful(attachedClient);
        assertThat(notifier.getAttachedClientsCount()).isEqualTo(1);
    }

    @Test
    public void shouldNotSendAMessageToDisabledClient() throws Exception {
        // given
        Object messageBody = new Object();

        Notifier notifier = new LocalNotifier();
        AttachedClient attachedClient = mock(AttachedClient.class);
        doReturn(false).when(attachedClient).isEnabled();
        notifier.attachClient(attachedClient);

        // when
        notifier.sendMessage(messageBody);

        // then
        verify(attachedClient, new Times(0)).sendMessage(messageBody, notifier.getCallback());
        assertThat(notifier.getAttachedClientsCount()).isEqualTo(1);
    }

    @Test
    public void shouldRemoveAttachedClientWhenItGetsAnException() throws Exception {

        ArgumentCaptor<MessageCallback> messageCallback = ArgumentCaptor.forClass(MessageCallback.class);

        // given
        Notifier notifier = new LocalNotifier();

        AttachedClient attachedClient = mock(AttachedClient.class);
        doReturn(true).when(attachedClient).isEnabled();

        notifier.attachClient(attachedClient);

        // when
        notifier.sendMessage(new Object());

        // then
        verify(attachedClient).sendMessage(any(), messageCallback.capture());

        messageCallback.getValue().failed(attachedClient, new Throwable());
        assertThat(notifier.getAttachedClientsCount()).isEqualTo(0);
    }

}