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
package org.jboss.pnc.integration.utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

@Slf4j
public class BPMWireMock implements Closeable {

    private final WireMockServer wireMockServer;

    private static int operationId = 42;

    public BPMWireMock(int port) {
        wireMockServer = new WireMockServer(port);
        wireMockServer.stubFor(
                any(urlMatching(".*")).willReturn(
                        aResponse().withStatus(201)
                                .withBody(String.valueOf(operationId++))
                                .withHeader("Content-Type", "application/json")));
        wireMockServer.start();
    }

    public WireMockServer getWireMockServer() {
        return wireMockServer;
    }

    @Override
    public void close() throws IOException {
        wireMockServer.stop();
    }
}
