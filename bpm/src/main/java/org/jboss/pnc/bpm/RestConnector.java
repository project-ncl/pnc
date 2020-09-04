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
package org.jboss.pnc.bpm;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.spi.exception.ProcessManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matej Lazar
 */
public class RestConnector implements Connector {

    private static final Logger log = LoggerFactory.getLogger(RestConnector.class);
    private final HttpConfig httpConfig;

    private CloseableHttpClient httpClient;
    private final EndpointUrlResolver endpointUrl;

    public RestConnector(BpmModuleConfig bpmConfig) {
        httpConfig = new HttpConfig(
                bpmConfig.getHttpConnectionRequestTimeout(),
                bpmConfig.getHttpConnectTimeout(),
                bpmConfig.getHttpSocketTimeout());
        endpointUrl = new EndpointUrlResolver(bpmConfig.getBpmNewBaseUrl(), bpmConfig.getBpmNewDeploymentId());

        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        String username = bpmConfig.getBpmNewUsername();
        if (!StringUtils.isEmpty(username)) {
            CredentialsProvider provider = new BasicCredentialsProvider();
            provider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(username, bpmConfig.getBpmNewPassword()));
            httpClientBuilder.setDefaultCredentialsProvider(provider);
        }
        httpClient = httpClientBuilder.build();
    }

    @Override
    public Long startProcess(String processId, Map<String, Object> processParameters, String accessToken)
            throws ProcessManagerException {
        String url = endpointUrl.get(processId);
        log.debug("Staring new process using http endpoint: {}", url);

        Map<String, Map<String, Object>> body = new HashMap<>();
        body.put("in_initData", processParameters);

        HttpEntity requestEntity;
        try {
            requestEntity = new StringEntity(JsonOutputConverterMapper.apply(body));
        } catch (UnsupportedEncodingException e) {
            throw new ProcessManagerException("Cannot prepare BPM REST call.", e);
        }
        HttpPost request = new HttpPost(url);
        request.setEntity(requestEntity);
        configureRequest(accessToken, request);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 201) {
                Long processInstanceId = JsonOutputConverterMapper
                        .readValue(response.getEntity().getContent(), Long.class);
                log.info("Started new process instance with id: {}", processInstanceId);
                return processInstanceId;
            } else {
                throw new ProcessManagerException("Cannot start new process instance, response status: " + statusCode);
            }
        } catch (IOException e) {
            throw new ProcessManagerException("Cannot start new process instance.", e);
        }
    }

    private void configureRequest(String accessToken, HttpPost request) {
        request.setConfig(httpClientConfig().build());
        request.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
    }

    private RequestConfig.Builder httpClientConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(httpConfig.getConnectionRequestTimeout())
                .setConnectTimeout(httpConfig.getConnectTimeout())
                .setSocketTimeout(httpConfig.getSocketTimeout());
    }

    @Override
    public boolean isProcessInstanceCompleted(Long processInstanceId) {
        log.warn("Use direct removal instead of scheduled cleanup. ProcessInstanceId: {}", processInstanceId);
        return false;
    }

    @Override
    public boolean cancel(Long processInstanceId) {
        return false; // TODO
    }

    @Override
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            log.warn("Cannot close http client.", e);
        }
    }

    private static class EndpointUrlResolver {

        private final String baseUrl;
        private final String deploymentId;

        public EndpointUrlResolver(String baseUrl, String deploymentId) {
            this.baseUrl = baseUrl;
            this.deploymentId = deploymentId;
        }

        public String get(String processId) {
            return baseUrl + deploymentId + "/processes/" + processId + "/instances";
        }
    }
}
