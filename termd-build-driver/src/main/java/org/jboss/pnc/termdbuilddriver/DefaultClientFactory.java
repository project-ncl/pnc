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
package org.jboss.pnc.termdbuilddriver;

import org.jboss.pnc.buildagent.api.ResponseMode;
import org.jboss.pnc.buildagent.api.TaskStatusUpdateEvent;
import org.jboss.pnc.buildagent.client.BuildAgentClient;
import org.jboss.pnc.buildagent.client.BuildAgentClientException;
import org.jboss.pnc.buildagent.client.BuildAgentHttpClient;
import org.jboss.pnc.buildagent.client.BuildAgentSocketClient;
import org.jboss.pnc.buildagent.client.HttpClientConfiguration;
import org.jboss.pnc.buildagent.client.SocketClientConfiguration;
import org.jboss.pnc.buildagent.common.http.HttpClient;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.TermdBuildDriverModuleConfig;
import org.jboss.pnc.termdbuilddriver.transfer.DefaultFileTranser;
import org.jboss.pnc.termdbuilddriver.transfer.FileTranser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class DefaultClientFactory implements ClientFactory {

    private static final Logger logger = LoggerFactory.getLogger(DefaultClientFactory.class);

    /**
     * Connect timeout in millis. See {@link java.net.URLConnection#setConnectTimeout(int)}
     */
    private final Optional<Integer> fileTransferConnectTimeout;

    /**
     * Connect timeout in millis. See {@link java.net.URLConnection#setReadTimeout(int)}
     */
    private final Optional<Integer> fileTransferReadTimeout;
    private final String pncBaseUrl;

    private HttpClient httpClient;

    @Inject
    public DefaultClientFactory(TermdBuildDriverModuleConfig config, GlobalModuleGroup globalConfig) {
        fileTransferConnectTimeout = Optional.ofNullable(config.getFileTransferConnectTimeout());
        fileTransferReadTimeout = Optional.ofNullable(config.getFileTransferReadTimeout());
        pncBaseUrl = globalConfig.getPncUrl();
    }

    @PostConstruct
    public void init() throws IOException {
        httpClient = new HttpClient();
        logger.info("DefaultClientFactory initialized.");
    }

    @Override
    public BuildAgentClient createWebSocketBuildAgentClient(
            String terminalUrl,
            Consumer<TaskStatusUpdateEvent> onStatusUpdate)
            throws TimeoutException, InterruptedException, BuildAgentClientException {

        SocketClientConfiguration configuration = SocketClientConfiguration.newBuilder()
                .termBaseUrl(terminalUrl)
                .responseMode(ResponseMode.SILENT)
                .readOnly(false)
                .build();

        return new BuildAgentSocketClient(httpClient, Optional.empty(), onStatusUpdate, configuration);
    }

    @Override
    public BuildAgentClient createHttpBuildAgentClient(String terminalUrl) throws BuildAgentClientException {

        HttpClientConfiguration configuration = null;
        try {
            configuration = HttpClientConfiguration.newBuilder()
                    .termBaseUrl(terminalUrl)
                    .callbackMethod("POST")
                    .callbackUrl(new URL(pncBaseUrl + "/build-execution/completed"))
                    .livenessResponseTimeout(30000L)
                    .build();
        } catch (MalformedURLException e) {
            new BuildAgentClientException("Invalid callback URL.", e);
        }
        return new BuildAgentHttpClient(httpClient, configuration);
    }

    @Override
    public FileTranser getFileTransfer(URI baseServerUri, int maxLogSize) {
        DefaultFileTranser defaultFileTranser = new DefaultFileTranser(baseServerUri, maxLogSize);
        fileTransferConnectTimeout.ifPresent(defaultFileTranser::setConnectTimeout);
        fileTransferReadTimeout.ifPresent(defaultFileTranser::setReadTimeout);
        return defaultFileTranser;
    }

    @PreDestroy
    public void destroy() throws IOException {
        httpClient.close();
    }
}
