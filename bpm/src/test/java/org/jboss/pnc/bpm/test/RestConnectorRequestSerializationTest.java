/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.bpm.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.jboss.pnc.api.constants.MDCKeys;
import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.bpm.RestConnector;
import org.jboss.pnc.bpm.model.AnalyzeDeliverablesBpmRequest;
import org.jboss.pnc.bpm.task.AnalyzeDeliverablesTask;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.MDC;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RestConnectorRequestSerializationTest {

    private static final String DEPLOYMENT_ID = "/testing";

    @Mock
    private BpmModuleConfig bpmConfig;

    @Rule
    public WireMockRule wireMockServer = new WireMockRule(options().port(8081));

    private RestConnector connector;

    @Before
    public void before() {
        when(bpmConfig.getBpmNewBaseUrl()).thenReturn(wireMockServer.baseUrl());
        when(bpmConfig.getBpmNewDeploymentId()).thenReturn(DEPLOYMENT_ID);
        wireMockServer.stubFor(
                post(urlMatching(DEPLOYMENT_ID + ".*")).willReturn(
                        aResponse().withStatus(201)
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody("1")));
        this.connector = new RestConnector(bpmConfig);
    }

    @Test
    public void testSerialization() throws Exception {
        // with
        AnalyzeDeliverablesTask task = mockAnalyzeDeliverablesTask();
        MDC.put(MDCKeys.REQUEST_CONTEXT_KEY, "Hello");

        // when
        connector.startProcess("analyzer", task, "accessToken");

        // then
        verify(postRequestedFor(urlMatching(".*/analyzer/.*")));

        List<LoggedRequest> requests = findAll(postRequestedFor(urlMatching(".*/analyzer/.*")));
        assertThat(requests.size()).isOne();

        JsonNode body = JsonOutputConverterMapper.getMapper().readTree(requests.get(0).getBodyAsString());
        assertThat(body.get("initData")).isNotNull();
        assertThat(body.get("initData").get("mdc")).isNotNull();
        assertThat(body.get("initData").get("mdc").get(MDCKeys.REQUEST_CONTEXT_KEY)).isNotNull();
        assertThat(body.get("initData").get("mdc").get(MDCKeys.REQUEST_CONTEXT_KEY).textValue()).isNotNull()
                .isEqualTo("Hello");
        assertThat(body.get("initData").get("task")).isNotNull();
        assertThat(body.get("initData").get("task").get("request")).isNotNull();
        assertThat(body.get("initData").get("task").get("request").get("urls")).isNotEmpty();
        assertThat(body.get("initData").get("task").get("request").get("config")).isNotNull();
        assertThat(body.get("initData").get("task").get("callback")).isNotNull();

    }

    private AnalyzeDeliverablesTask mockAnalyzeDeliverablesTask() {
        String url1 = "http://hello.com";
        String url2 = "http://world.com";
        List<String> urls = new ArrayList<>();
        urls.add(url1);
        urls.add(url2);
        String config = "config";
        AnalyzeDeliverablesBpmRequest request = new AnalyzeDeliverablesBpmRequest(urls, config);
        Set<Request.Header> headers = new HashSet<>();
        headers.add(new Request.Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));

        Request callback = new Request(Request.Method.POST, URI.create("http://mock.com/"), headers);

        AnalyzeDeliverablesTask task = new AnalyzeDeliverablesTask(request, callback, "http://delanal.com/");
        return task;
    }
}
