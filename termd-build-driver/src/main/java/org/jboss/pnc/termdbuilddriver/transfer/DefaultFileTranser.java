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
package org.jboss.pnc.termdbuilddriver.transfer;

import org.jboss.pnc.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.function.Consumer;

public class DefaultFileTranser implements FileTranser {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String ENCODING = "UTF-8";
    private static final String UPLOAD_PATH = "servlet/upload";

    private final URI baseServerUri;

    private boolean fullyDownloaded = true;

    private int maxDownloadSize;

    /**
     * Connect timeout in millis. See {@link java.net.URLConnection#setConnectTimeout(int)}
     */
    private int connectTimeout = 5000;

    /**
     * Read timeout in millis. See {@link java.net.URLConnection#setReadTimeout(int)}
     */
    private int readTimeout = 30000;

    public DefaultFileTranser(URI baseServerUri, int maxDownloadSize) {
        this.baseServerUri = baseServerUri;
        this.maxDownloadSize = maxDownloadSize;
    }

    @Override
    public StringBuffer downloadFileToStringBuilder(StringBuffer logsAggregate, URI uri) throws TransferException {
        try {
            logger.debug("Downloading file to String Buffer from {}", uri);

            ArrayDeque<String> logLines = new ArrayDeque<>();

            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");

            connection.setDoOutput(true);
            connection.setDoInput(true);

            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);

            Consumer<String> removedLines = (removedLine) -> {
                fullyDownloaded = false;
                logger.debug("Dropped log line from URI {}: {}.", uri, removedLine);
            };

            try (InputStream inputStream = connection.getInputStream()) {
                Charset charset = Charset.forName(ENCODING);
                StringUtils.readStream(inputStream, charset, logLines, maxDownloadSize, removedLines);
            }

            logsAggregate.append("==== ").append(uri.toString()).append(" ====\n");
            while (true) {
                String line = logLines.pollFirst();
                if (line == null) {
                    break;
                }
                logsAggregate.append(line + "\n");
            }
            if (logLines.size() > 0) {
                logger.warn("Log buffer was not fully drained for URI: {}", uri);
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Downloaded log: {}.", logsAggregate);
            }
            return logsAggregate;
        } catch (IOException e) {
            throw new TransferException("Could not obtain log file: " + uri.toString(), e);
        }
    }

    @Override
    public boolean isFullyDownloaded() {
        return fullyDownloaded;
    }

    @Override
    public void uploadScript(String script, Path remoteFilePath) throws TransferException {
        logger.debug("Uploading build script to remote path {}, build script {}", remoteFilePath, script);
        String scriptPath = UPLOAD_PATH + remoteFilePath.toAbsolutePath().toString();
        logger.debug("Resolving script path {} to base uri {}", scriptPath, baseServerUri);
        URI uploadUri = baseServerUri.resolve(scriptPath);
        try {
            HttpURLConnection connection = (HttpURLConnection) uploadUri.toURL().openConnection();
            connection.setRequestMethod("PUT");

            connection.setDoOutput(true);
            connection.setDoInput(true);

            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);

            byte[] fileContent = script.getBytes();
            connection.setRequestProperty("Content-Length", "" + fileContent.length);

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(fileContent);
            }

            if (200 != connection.getResponseCode()) {
                throw new TransferException(
                        "Could not upload script to Build Agent at url " + connection.getURL()
                                + " - Returned status code " + connection.getResponseCode());
            }
            logger.debug("Uploaded successfully");
        } catch (IOException e) {
            throw new TransferException("Could not upload build script: " + uploadUri.toString(), e);
        }
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
}
