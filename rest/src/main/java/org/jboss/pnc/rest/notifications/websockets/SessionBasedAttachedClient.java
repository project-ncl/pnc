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

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.websocket.Session;

import org.jboss.pnc.spi.notifications.AttachedClient;
import org.jboss.pnc.spi.notifications.OutputConverter;

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
    public void sendMessage(Object messageBody)
            throws IOException, ExecutionException, InterruptedException, CancellationException {

        Future<Void> future = session.getAsyncRemote().sendText(outputConverter.apply(messageBody));
        // wait for completion (forever, no timeout)
        if (future.isDone()) {
            future.get();
        }

        // // EXAMPLE on How to wait only prescribed amount of time for the send to complete, cancelling the message if the timeout occurs.
        //
        // Future<Void> fut = null;
        // try
        // {
        // fut = session.getAsyncRemote().sendText(outputConverter.apply(messageBody));
        // // wait for completion (timeout)
        // fut.get(2,TimeUnit.SECONDS);
        // }
        // catch (ExecutionException | InterruptedException | CancellationException e)
        // {
        // // Send failed
        // e.printStackTrace();
        // throw e;
        // }
        // catch (TimeoutException e)
        // {
        // // timeout
        // e.printStackTrace();
        // if (fut != null)
        // {
        // // cancel the message
        // fut.cancel(true);
        // }
        // }
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
