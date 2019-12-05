/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
import io.undertow.util.HeaderValues;
import org.jboss.pnc.common.logging.MDCUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ClientTest {

    @Test
    public void shouldRetryFailedConnection() throws RemoteResourceException {

        AtomicInteger requestsReceived = new AtomicInteger(0);
        AtomicReference<String> headerReceived = new AtomicReference<>();

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(new HttpHandler() {
                    @Override
                    public void handleRequest(final HttpServerExchange exchange) throws Exception {
                        requestsReceived.incrementAndGet();
                        String headerName = MDCUtils.getMDCToHeaderMappings().get(MDCUtils.REQUEST_CONTEXT_KEY);
                        HeaderValues strings = exchange.getRequestHeaders().get(headerName);
                        if (strings != null) {
                            headerReceived.set(strings.getFirst());
                        }
                        exchange.getConnection().close();
                    }
                }).build();
        server.start();

        Configuration configuration = Configuration.builder()
                .protocol("http")
                .host("localhost")
                .port(8080)
//                .mdcToHeadersMappings(MDCUtils.getMDCToHeaderMappings())
                .addDefaultMdcToHeadersMappings()
                .build();

        ProjectClient projectClient = new ProjectClient(configuration);

        String requestContext = "12345";
        MDC.put(MDCUtils.REQUEST_CONTEXT_KEY, requestContext);
        try {
            projectClient.getSpecific("1");
        } catch (javax.ws.rs.ProcessingException | ClientException e) {
            //expected
        }

        Assert.assertTrue(requestsReceived.intValue() > 2);
        Assert.assertEquals(requestContext, headerReceived.get());
        System.out.println("Done!");
    }
}
