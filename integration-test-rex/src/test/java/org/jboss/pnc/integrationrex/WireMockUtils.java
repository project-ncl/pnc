/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integrationrex;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

import org.jboss.pnc.integrationrex.mock.LogJsonAction;
import org.wiremock.webhooks.WebhookDefinition;
import org.wiremock.webhooks.Webhooks;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.trafficlistener.ConsoleNotifyingWiremockNetworkTrafficListener;

public class WireMockUtils {
    public static WebhookDefinition baseBPMWebhook() {
        return new WebhookDefinition().withMethod(RequestMethod.POST)
                .withHeader("Content-Type", "application/json")
                .withHeader("Authorization", "{{originalRequest.headers.Authorization}}")
                .withUrl("{{jsonPath originalRequest.body '$.callback.uri'}}");
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
