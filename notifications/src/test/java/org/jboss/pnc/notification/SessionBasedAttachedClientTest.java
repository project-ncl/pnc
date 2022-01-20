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
package org.jboss.pnc.notification;

import org.junit.Test;

import javax.websocket.Session;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

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