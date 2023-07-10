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
package org.jboss.pnc.integrationrex;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.trafficlistener.ConsoleNotifyingWiremockNetworkTrafficListener;
import org.jboss.pnc.integrationrex.mock.LogJsonAction;
import org.wiremock.webhooks.WebhookDefinition;
import org.wiremock.webhooks.Webhooks;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

public class WireMockUtils {
    public static WebhookDefinition baseBPMWebhook() {
        return new WebhookDefinition().withMethod(RequestMethod.POST)
                .withHeader("Content-Type", "application/json")
                .withHeader("Authorization", "{{originalRequest.headers.Authorization}}")
                .withUrl("{{jsonPath originalRequest.body '$.callback'}}");
    }

    public static ResponseDefinitionBuilder response200() {
        return aResponse().withStatus(200).withHeader("Content-Type", "application/json");
    }

    public static WireMockConfiguration defaultConfiguration(int port) {
        return WireMockConfiguration.options()
                .networkTrafficListener(new ConsoleNotifyingWiremockNetworkTrafficListener())
                .port(port)
                .extensions(LogJsonAction.class)
                .extensions(ResponseTemplateTransformer.builder().global(false).maxCacheEntries(0L).build())
                .extensions(Webhooks.class);
    }
}
