package org.jboss.pnc.rest.notifications.websockets;

import org.jboss.pnc.rest.notifications.OutputConverter;
import org.junit.Test;

import javax.websocket.Session;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class SessionBasedAttachedClientTest {

    @Test
    public void shouldTwoInstancesCreatedTheSameWayBeEqual() throws Exception {
        //given
        Session session = mock(Session.class);
        OutputConverter converter = new JSonOutputConverter();

        SessionBasedAttachedClient client1 = new SessionBasedAttachedClient(session, converter);
        SessionBasedAttachedClient client2 = new SessionBasedAttachedClient(session, converter);

        //when//then
        assertEquals(client1, client2);
    }

}