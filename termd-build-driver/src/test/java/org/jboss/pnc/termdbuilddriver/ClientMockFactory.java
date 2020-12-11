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

import org.jboss.pnc.buildagent.api.TaskStatusUpdateEvent;
import org.jboss.pnc.buildagent.client.BuildAgentClient;
import org.jboss.pnc.buildagent.client.BuildAgentClientException;
import org.jboss.pnc.buildagent.common.http.HttpClient;
import org.jboss.pnc.buildagent.common.http.StringResult;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ClientMockFactory implements ClientFactory {

    BuildAgentMockClient buildAgentClient;

    private Consumer<TaskStatusUpdateEvent> onStatusUpdate;

    public ClientMockFactory() {
        buildAgentClient = new BuildAgentMockClient();
    }

    public BuildAgentMockClient getBuildAgentClient() {
        return buildAgentClient;
    }

    @Override
    public BuildAgentClient createWebSocketBuildAgentClient(
            String terminalUrl,
            Consumer<TaskStatusUpdateEvent> onStatusUpdate)
            throws TimeoutException, InterruptedException, BuildAgentClientException {
        this.onStatusUpdate = onStatusUpdate;
        return buildAgentClient;
    }

    @Override
    public BuildAgentClient createHttpBuildAgentClient(
            String terminalUrl,
            String executionId,
            Map<String, String> callbackHeaders) throws BuildAgentClientException {
        return new BuildAgentMockClient();
    }

    public Consumer<TaskStatusUpdateEvent> getOnStatusUpdate() {
        return onStatusUpdate;
    }

    public class BuildAgentMockClient implements BuildAgentClient {

        private boolean serverAlive;

        private List<Object> executedCommands = new ArrayList<>();

        @Override
        public void execute(Object command) throws BuildAgentClientException {
            executedCommands.add(command);
        }

        @Override
        public void execute(Object command, long responseTimeout, TimeUnit unit) throws BuildAgentClientException {
            executedCommands.add(command);
        }

        @Override
        public void uploadFile(
                ByteBuffer buffer,
                Path remoteFilePath,
                CompletableFuture<HttpClient.Response> responseFuture) {
            responseFuture.complete(new HttpClient.Response(200, new StringResult(true, "")));
        }

        @Override
        public void downloadFile(Path remoteFilePath, CompletableFuture<HttpClient.Response> responseFuture) {
            responseFuture.complete(new HttpClient.Response(200, new StringResult(true, "Mock log.")));
        }

        @Override
        public void downloadFile(
                Path remoteFilePath,
                CompletableFuture<HttpClient.Response> responseFuture,
                long maxDownloadSize) {
            responseFuture.complete(new HttpClient.Response(200, new StringResult(true, "Mock log.")));
        }

        @Override
        public void cancel() throws BuildAgentClientException {
        }

        @Override
        public String getSessionId() {
            return null;
        }

        @Override
        public boolean isServerAlive() {
            return serverAlive;
        }

        @Override
        public void close() throws IOException {

        }

        public void setServerAlive(boolean serverAlive) {
            this.serverAlive = serverAlive;
        }

        public List<Object> getExecutedCommands() {
            return executedCommands;
        }
    }
}
