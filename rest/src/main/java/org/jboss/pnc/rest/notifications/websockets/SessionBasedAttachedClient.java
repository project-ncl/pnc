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
package org.jboss.pnc.rest.notifications.websockets;

import org.jboss.pnc.rest.notifications.AttachedClient;
import org.jboss.pnc.rest.notifications.OutputConverter;

import javax.websocket.Session;
import java.io.IOException;

public class SessionBasedAttachedClient implements AttachedClient {

    private final Session session;
    private final OutputConverter outputConverter;

    public SessionBasedAttachedClient(Session session, OutputConverter outputConverter) {
        this.session = session;
        this.outputConverter = outputConverter;
    }

    @Override
    public boolean isEnabled() {
        return session.isOpen();
    }

    @Override
    public void sendMessage(Object messageBody) throws IOException {
        session.getBasicRemote().sendText(outputConverter.apply(messageBody));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SessionBasedAttachedClient that = (SessionBasedAttachedClient) o;

        if (outputConverter != null ? !outputConverter.equals(that.outputConverter) : that.outputConverter != null)
            return false;
        if (session != null ? !session.equals(that.session) : that.session != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = session != null ? session.hashCode() : 0;
        result = 31 * result + (outputConverter != null ? outputConverter.hashCode() : 0);
        return result;
    }
}
