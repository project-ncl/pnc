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
package org.jboss.pnc.termdbuilddriver.transfer;

import org.jboss.pnc.buildagent.client.BuildAgentClient;
import org.jboss.pnc.buildagent.common.http.HttpClient;
import org.jboss.pnc.buildagent.common.http.StringResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ClientFileTransfer implements FileTransfer {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private BuildAgentClient buildAgentClient;

    private boolean fullyDownloaded = true;

    private int maxDownloadSize;

    private int readTimeout = 30000;

    public ClientFileTransfer(BuildAgentClient buildAgentClient, int maxDownloadSize) {
        this.buildAgentClient = buildAgentClient;
        this.maxDownloadSize = maxDownloadSize;
    }

    @Override
    public StringBuffer downloadFileToStringBuilder(StringBuffer logsAggregate, String path) throws TransferException {
        try {
            logger.debug("Downloading file to String Buffer from {}", path);

            CompletableFuture<HttpClient.Response> responseFuture = buildAgentClient
                    .downloadFile(Paths.get(path), maxDownloadSize);

            HttpClient.Response response = responseFuture.get(readTimeout, TimeUnit.MILLISECONDS);

            logsAggregate.append("==== ").append(path).append(" ====\n");

            StringResult stringResult = response.getStringResult();
            logsAggregate.append(stringResult.getString());

            if (!stringResult.isComplete()) {
                logger.warn("\nLog buffer was not fully drained for URI: {}", path);
                fullyDownloaded = false;
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Downloaded log: {}.", logsAggregate);
            }
            return logsAggregate;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new TransferException("Could not obtain log file: " + path, e);
        }
    }

    @Override
    public boolean isFullyDownloaded() {
        return fullyDownloaded;
    }

    @Override
    public void uploadScript(String script, Path remoteFilePath) throws TransferException {
        logger.debug("Uploading build script to remote path {}, build script {}", remoteFilePath, script);
        CompletableFuture<HttpClient.Response> responseFuture = buildAgentClient
                .uploadFile(ByteBuffer.wrap(script.getBytes(StandardCharsets.UTF_8)), remoteFilePath);
        try {
            HttpClient.Response response = responseFuture.get(readTimeout, TimeUnit.MILLISECONDS);
            if (response.getCode() != 200) {
                throw new TransferException("Failed to upload script. Response status: " + response.getCode());
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new TransferException("Failed to upload script.", e);
        }
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
}
