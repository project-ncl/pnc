/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.notification;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import javax.websocket.Session;

import org.junit.Test;

public class SessionBasedAttachedClientTest {

    @Test
    public void shouldTwoInstancesCreatedTheSameWayBeEqual() throws Exception {
        // given
        Session session = mock(Session.class);

        SessionBasedAttachedClient client1 = new SessionBasedAttachedClient(session);
        SessionBasedAttachedClient client2 = new SessionBasedAttachedClient(session);

        // when//then
        assertEquals(client1, client2);
    }

}