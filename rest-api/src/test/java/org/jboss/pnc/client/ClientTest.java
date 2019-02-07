/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.client;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ClientTest {

    @Test
    public void shouldRetryFailedConnection() throws RemoteResourceException {

        AtomicInteger requestsReceived = new AtomicInteger(0);

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(new HttpHandler() {
                    @Override
                    public void handleRequest(final HttpServerExchange exchange) throws Exception {
                        requestsReceived.incrementAndGet();
                        exchange.getConnection().close();
                    }
                }).build();
        server.start();

        Configuration connectionInfo = Configuration.builder()
                .protocol("http")
                .host("localhost")
                .port(8080)
                .build();
        ProjectClient projectClient = new ProjectClient(connectionInfo);
        try {
            projectClient.getSpecific(1);
        } catch (javax.ws.rs.ProcessingException e) {
            //expected
        }

        Assert.assertTrue(requestsReceived.intValue() > 2);

    }
}
