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
package org.jboss.pnc.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.undertow.Undertow;
import io.undertow.util.HeaderValues;
import org.jboss.pnc.api.constants.MDCKeys;
import org.jboss.pnc.common.logging.MDCUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.MDC;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ClientTest {

    @Rule
    public WireMockRule wireMockServer = new WireMockRule(options().port(8081));

    @Test
    public void shouldRetryFailedConnection() throws RemoteResourceException {

        AtomicInteger requestsReceived = new AtomicInteger(0);
        AtomicReference<String> headerReceived = new AtomicReference<>();

        Undertow server = Undertow.builder().addHttpListener(8080, "localhost").setHandler(exchange -> {
            requestsReceived.incrementAndGet();
            String headerName = MDCUtils.HEADER_KEY_MAPPING.get(MDCKeys.REQUEST_CONTEXT_KEY);
            HeaderValues strings = exchange.getRequestHeaders().get(headerName);
            if (strings != null) {
                headerReceived.set(strings.getFirst());
            }
            exchange.getConnection().close();
        }).build();
        server.start();
        try {
            Configuration configuration = getBasicConfiguration(8080).addDefaultMdcToHeadersMappings().build();

            ProjectClient projectClient = new ProjectClient(configuration);

            String requestContext = "12345";
            MDC.put(MDCKeys.REQUEST_CONTEXT_KEY, requestContext);
            try {
                projectClient.getSpecific("1");
            } catch (javax.ws.rs.ProcessingException | ClientException e) {
                // expected
            }

            Assert.assertTrue(requestsReceived.intValue() > 2);
            Assert.assertEquals(requestContext, headerReceived.get());
            System.out.println("Done!");

        } finally {
            server.stop();
        }
    }

    @Test
    public void shouldRefreshTokenIfNotAuthorized() throws RemoteResourceException {
        final TokenGenerator tokenGenerator = new TokenGenerator();

        // given
        Configuration configuration = getBasicConfiguration(8081).bearerTokenSupplier(tokenGenerator::getToken).build();

        wireMockServer.stubFor(
                get(urlMatching(".*")).willReturn(
                        aResponse().withStatus(401).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)));

        BuildClient buildClient = new BuildClient(configuration);

        // when
        try {
            buildClient.getSpecific("1");
        } catch (RemoteResourceException e) {
            // We are returning 401 also after token refresh
            Assert.assertTrue(e.getCause() instanceof NotAuthorizedException);
        }

        // then
        Assert.assertEquals(2, tokenGenerator.getInvocationCount());
    }

    private Configuration.ConfigurationBuilder getBasicConfiguration(int port) {
        return Configuration.builder().protocol("http").host("localhost").port(port);
    }

    private class TokenGenerator {

        private int invocationCount = 0;

        public String getToken() {
            return "TOKEN-" + ++invocationCount;
        }

        public int getInvocationCount() {
            return invocationCount;
        }
    }
}
