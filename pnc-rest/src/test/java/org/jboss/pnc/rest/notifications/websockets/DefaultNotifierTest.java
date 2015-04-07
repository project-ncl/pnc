package org.jboss.pnc.rest.notifications.websockets;

import org.jboss.pnc.rest.notifications.AttachedClient;
import org.jboss.pnc.rest.notifications.Notifier;
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