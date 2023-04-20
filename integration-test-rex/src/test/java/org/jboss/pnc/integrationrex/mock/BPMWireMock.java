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
package org.jboss.pnc.integrationrex.mock;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.trafficlistener.ConsoleNotifyingWiremockNetworkTrafficListener;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.extern.slf4j.Slf4j;
import org.wiremock.webhooks.Webhooks;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Slf4j
public class BPMWireMock extends WireMockRule {
    public BPMWireMock(int port) {
        super(
                WireMockConfiguration.options()
                        .networkTrafficListener(new ConsoleNotifyingWiremockNetworkTrafficListener())
                        .port(port)
                        .extensions(LogJsonAction.class)
                        .extensions(Webhooks.class));
        stubFor(
                any(urlMatching(".*")).atPriority(10)
                        .willReturn(
                                aResponse().withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("{\"status\":\"success\"}"))
                        .withPostServeAction("log-json", new Parameters()));
    }
}
