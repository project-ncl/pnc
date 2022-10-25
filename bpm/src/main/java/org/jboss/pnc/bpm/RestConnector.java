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
package org.jboss.pnc.bpm;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.jboss.pnc.api.constants.MDCHeaderKeys;
import org.jboss.pnc.api.constants.MDCKeys;
import org.jboss.pnc.bpm.model.MDCParameters;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.spi.exception.ProcessManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Matej Lazar
 */
public class RestConnector implements Connector {

    private static final Logger log = LoggerFactory.getLogger(RestConnector.class);
    private final HttpConfig httpConfig;

    private CloseableHttpClient httpClient;
    private final EndpointUrlResolver endpointUrl;

    /**
     * Container id used to start new process instances. To support live process updates, query for the instance first
     * and get the container id when managing active instances.
     */
    private String currentDeploymentId;

    public RestConnector(BpmModuleConfig bpmConfig) {
        httpConfig = new HttpConfig(
                bpmConfig.getHttpConnectionRequestTimeout(),
                bpmConfig.getHttpConnectTimeout(),
                bpmConfig.getHttpSocketTimeout());
        endpointUrl = new EndpointUrlResolver(bpmConfig.getBpmNewBaseUrl());

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
        currentDeploymentId = bpmConfig.getBpmNewDeploymentId();
    }

    @Override
    public Long startProcess(String processId, Object requestObject, String accessToken)
            throws ProcessManagerException {
        return startProcess(processId, requestObject, Sequence.nextBase32Id(), accessToken);
    }

    @WithSpan()
    public Long startProcess(
            @SpanAttribute(value = "processId") String processId,
            @SpanAttribute(value = "requestObject") Object requestObject,
            @SpanAttribute(value = "correlationKey") String correlationKey,
            String accessToken) throws ProcessManagerException {
        HttpPost request = endpointUrl.startProcessInstance(currentDeploymentId, processId, correlationKey);
        log.debug("Starting new process using http endpoint: {}", request.getURI());

        Map<String, Object> processParameters = new HashMap<>();
        processParameters.put("auth", Collections.singletonMap("token", accessToken));
        processParameters.put("mdc", new MDCParameters());
        processParameters.put("task", requestObject);

        Map<String, Map<String, Object>> body = Collections.singletonMap("initData", processParameters);

        HttpEntity requestEntity;
        try {
            requestEntity = new StringEntity(JsonOutputConverterMapper.apply(body));
        } catch (UnsupportedEncodingException e) {
            throw new ProcessManagerException("Cannot prepare BPM REST call.", e);
        }
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

    private void configureRequest(String accessToken, HttpRequestBase request) {
        request.setConfig(httpClientConfig().build());
        request.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        // Adding OTEL headers for distributed tracing
        MDCUtils.getCustomContext(MDCKeys.SLF4J_TRACE_ID_KEY).ifPresent(v -> {
            log.debug("Setting {} header: {}", MDCKeys.SLF4J_TRACE_ID_KEY, v);
            request.addHeader(MDCHeaderKeys.SLF4J_TRACE_ID.getHeaderName(), v);
        });
        MDCUtils.getCustomContext(MDCKeys.SLF4J_SPAN_ID_KEY).ifPresent(v -> {
            log.debug("Setting {} header: {}", MDCKeys.SLF4J_SPAN_ID_KEY, v);
            request.addHeader(MDCHeaderKeys.SLF4J_SPAN_ID.getHeaderName(), v);
        });
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
    public boolean cancelByCorrelation(String correlationKey, String accessToken) {
        try {
            HttpGet request = endpointUrl.queryProcessInstanceByCorrelation(correlationKey);
            log.debug("Querying process instance using http endpoint: {}", request.getURI());
            Optional<RestProcessInstance> instance = doQueryProcessInstance(accessToken, request);
            if (instance.isPresent()) {
                log.info("Found process instance id:{} invoking cancel.", instance.get().getId());
                return doCancel(instance.get(), accessToken);
            } else {
                log.debug("Did not found process instance for correlationKey: {} to invoke cancel.", correlationKey);
                return false;
            }
        } catch (RestConnectorException e) {
            log.error("Cannot query process instance.", e);
            return false;
        }
    }

    @Override
    public boolean cancel(Long processInstanceId, String accessToken) {
        try {
            HttpGet request = endpointUrl.queryProcessInstance(Long.toString(processInstanceId));
            log.debug("Querying process instance using http endpoint: {}", request.getURI());
            Optional<RestProcessInstance> instance = doQueryProcessInstance(accessToken, request);
            if (instance.isPresent()) {
                return doCancel(instance.get(), accessToken);
            } else {
                return false;
            }
        } catch (RestConnectorException e) {
            log.error("Cannot query process instance.", e);
            return false;
        }
    }

    private boolean doCancel(RestProcessInstance processInstance, String accessToken) {
        HttpPost request = endpointUrl.processInstanceSignal(
                processInstance.getContainerId(),
                Long.toString(processInstance.getId()),
                "CancelAll");
        log.debug("Cancelling process instance using http endpoint: {}", request.getURI());

        configureRequest(accessToken, request);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                log.info("Cancelled process instance id: {}", processInstance.getId());
                return true;
            } else {
                log.warn("Cannot cancel process instance, response status: {}", statusCode);
                return false;
            }
        } catch (IOException e) {
            log.error("Cannot cancel process instance.", e);
            return false;
        }
    }

    private Optional<RestProcessInstance> doQueryProcessInstance(String accessToken, HttpGet request)
            throws RestConnectorException {
        configureRequest(accessToken, request);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                try (InputStream entity = response.getEntity().getContent()) {
                    RestProcessInstance processInstance = JsonOutputConverterMapper.getMapper()
                            .readValue(entity, RestProcessInstance.class);
                    return Optional.ofNullable(processInstance);
                } catch (Exception e) {
                    throw new RestConnectorException("Cannot read process instance response.", e);
                }
            } else {
                throw new RestConnectorException("Cannot query process instance, response status: " + statusCode);
            }
        } catch (IOException e) {
            throw new RestConnectorException("Cannot query process instance.", e);
        }
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

        /**
         * Base url of BPM. It shouldn't end with a trailing slash
         */
        private final String baseUrl;

        public EndpointUrlResolver(String baseUrl) {

            // Remove trailing slash if present
            if (baseUrl != null && baseUrl.endsWith("/")) {
                this.baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            } else {
                this.baseUrl = baseUrl;
            }
        }

        public HttpPost startProcessInstance(String deploymentId, String processId) {
            return new HttpPost(baseUrl + "/containers/" + deploymentId + "/processes/" + processId + "/instances");
        }

        public HttpPost startProcessInstance(String deploymentId, String processId, String correlationKey) {
            return new HttpPost(
                    baseUrl + "/containers/" + deploymentId + "/processes/" + processId + "/instances/correlation/"
                            + correlationKey);
        }

        public HttpPost processInstanceSignal(String deploymentId, String processInstanceId, String signal) {
            return new HttpPost(
                    baseUrl + "/containers/" + deploymentId + "/processes/instances/" + processInstanceId + "/signal/"
                            + signal);
        }

        public HttpGet queryProcessInstance(String processInstanceId) {
            return new HttpGet(baseUrl + "/queries/processes/instances/" + processInstanceId);
        }

        public HttpGet queryProcessInstanceByCorrelation(String correlationKey) {
            return new HttpGet(baseUrl + "/queries/processes/instance/correlation/" + correlationKey);
        }

        public String queryProcessInstances(String processId) {
            return baseUrl + "/queries/processes/" + processId + "/instances";
        }
    }
}
