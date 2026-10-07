/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integration.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import java.io.Closeable;
import java.io.IOException;

import com.github.tomakehurst.wiremock.WireMockServer;

import lombok.extern.slf4j.Slf4j;

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
