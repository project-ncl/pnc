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

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.spi.exception.CoreException;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.services.client.api.RemoteRuntimeEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import static org.jboss.pnc.bpm.BpmManager.AUTHENTICATION_TIMEOUT_S;
import static org.kie.api.runtime.process.ProcessInstance.STATE_ABORTED;
import static org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED;

/**
 * @author Matej Lazar
 */
public class KieClientConnector implements Connector {

    private static final Logger log = LoggerFactory.getLogger(KieClientConnector.class);

    private final KieSession session;
    private final String cancelEndpointUrl;
    private final HttpConfig httpConfig;

    public KieClientConnector(GlobalModuleGroup globalConfig, BpmModuleConfig bpmConfig) throws CoreException {
        session = initKieSession(globalConfig, bpmConfig);
        cancelEndpointUrl = StringUtils.stripEndingSlash(globalConfig.getBpmUrl()) + "/nclcancelhandler";
        httpConfig = new HttpConfig(
                bpmConfig.getHttpConnectionRequestTimeout(),
                bpmConfig.getHttpConnectTimeout(),
                bpmConfig.getHttpSocketTimeout());
    }

    private KieSession initKieSession(GlobalModuleGroup globalConfig, BpmModuleConfig bpmConfig) throws CoreException {
        RuntimeEngine restSessionFactory;
        try {
            restSessionFactory = RemoteRuntimeEngineFactory.newRestBuilder()
                    .addDeploymentId(bpmConfig.getDeploymentId())
                    .addUrl(new URL(globalConfig.getBpmUrl()))
                    .addUserName(bpmConfig.getUsername())
                    .addPassword(bpmConfig.getPassword())
                    .addTimeout(AUTHENTICATION_TIMEOUT_S)
                    .build();
        } catch (Exception e) {
            throw new CoreException(
                    "Could not initialize connection to BPM server at '" + globalConfig.getBpmUrl()
                            + "' check that the URL is correct.",
                    e);
        }

        return restSessionFactory.getKieSession();
    }

    @Override
    public void close() {
        session.dispose();
    }

    @Override
    public Long startProcess(String processId, Map<String, Object> processParameters, String accessToken) {
        ProcessInstance processInstance = session.startProcess(processId, processParameters);

        if (processInstance == null) {
            log.warn("Failed to create new process instance.");
            return -1L;
        }
        return processInstance.getId();
    }

    @Override
    public boolean isProcessInstanceCompleted(Long processInstanceId) {
        ProcessInstance processInstance = session.getProcessInstance(processInstanceId);
        log.debug("fetched: {}", processInstance);
        if (processInstance == null) {
            return true;
        }
        int state = processInstance.getState();
        return state == STATE_COMPLETED || state == STATE_ABORTED;
    }

    @Override
    public boolean cancel(Long processInstanceId, String accessToken) {

        URI uri;
        try {
            URIBuilder uriBuilder = new URIBuilder(cancelEndpointUrl);
            uriBuilder.addParameter("processInstanceId", processInstanceId.toString());
            uri = uriBuilder.build();
        } catch (URISyntaxException e) {
            log.error("Unable to cancel process id: " + processInstanceId, e);
            return false;
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            log.debug("Triggering the cancellation using url: {}", uri.toString());
            HttpGet httpget = new HttpGet(uri);
            httpget.setConfig(
                    RequestConfig.custom()
                            .setConnectionRequestTimeout(httpConfig.getConnectionRequestTimeout())
                            .setConnectTimeout(httpConfig.getConnectTimeout())
                            .setSocketTimeout(httpConfig.getSocketTimeout())
                            .build());
            CloseableHttpResponse httpResponse = httpClient.execute(httpget);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            httpResponse.close();
            log.info(
                    "Cancel request for processInstanceId: {} completed with status: {}.",
                    processInstanceId,
                    statusCode);
            return statusCode == 200;
        } catch (IOException e) {
            log.error("Unable to cancel process id: " + processInstanceId, e);
            return false;
        }
    }
}
