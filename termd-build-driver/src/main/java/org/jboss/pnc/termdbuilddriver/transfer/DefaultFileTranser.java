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

    /**
     * Stupid functional interface to encapsulate upload / download function. Used for the retry method
     */
    @FunctionalInterface
    interface MyRunnable<T> {
        T run() throws TransferException;
    }

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

    /**
     * Max number of http attempts in case of failure
     */
    private int httpRetryMaxAttempts = 10;

    /**
     * How long to wait between retry attempts
     */
    private long httpRetryWaitBeforeRetry = 500L;

    public DefaultFileTranser(URI baseServerUri, int maxDownloadSize) {
        this.baseServerUri = baseServerUri;
        this.maxDownloadSize = maxDownloadSize;
    }

    @Override
    public StringBuffer downloadFileToStringBuilder(StringBuffer logsAggregate, URI uri) throws TransferException {
        return retry(() -> downloadFileToStringBuilderPrivate(logsAggregate, uri), "Download log file");
    }

    private StringBuffer downloadFileToStringBuilderPrivate(StringBuffer logsAggregate, URI uri)
            throws TransferException {
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
        retry(() -> uploadScriptPrivate(script, remoteFilePath), "Upload script");
    }

    private Void uploadScriptPrivate(String script, Path remoteFilePath) throws TransferException {
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

            return null;
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

    public void setHttpRetryMaxAttempts(int httpRetryMaxAttempts) {
        this.httpRetryMaxAttempts = httpRetryMaxAttempts;
    }

    public int getHttpRetryMaxAttempts() {
        return httpRetryMaxAttempts;
    }

    public void setHttpRetryWaitBeforeRetry(long httpRetryWaitBeforeRetry) {
        this.httpRetryWaitBeforeRetry = httpRetryWaitBeforeRetry;
    }

    public long getHttpRetryWaitBeforeRetry() {
        return httpRetryWaitBeforeRetry;
    }

    /**
     * Helper method to sleep the thread for x milliseconds. Written so that we don't have to deal with exceptions
     *
     * @param milliseconds amount of sleep
     */
    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException f) {
            logger.warn("Could not sleep peacefully!");
        }
    }

    /**
     * Retry to run the myRunnable if a transfer exception is thrown, up until httpRetryMaxAttempts. Sleep for
     * httpRetryWaitBeforeRetry milliseconds between retries.
     *
     * If we have reached the max number of attempts, rethrow the transfer exception
     *
     * @param myRunnable task to run
     * @param taskName task name to use for logging
     * @throws TransferException thrown if the task fails after httpRetryMaxAttempts
     */
    <T> T retry(MyRunnable<T> myRunnable, String taskName) throws TransferException {

        // make sure we attempt to call the runnable at least once!
        int tempHttpRetryMaxAttempts = httpRetryMaxAttempts >= 1 ? httpRetryMaxAttempts : 1;

        for (int i = 1; i <= tempHttpRetryMaxAttempts; i++) {

            try {
                return myRunnable.run();
            } catch (TransferException e) {
                // if we have reached the max number of attempts, give up and throw the transfer exception
                if (i == tempHttpRetryMaxAttempts) {
                    throw e;
                } else {
                    logger.warn("{} failed after {} attempts. Sleeping...", taskName, i, e);
                    sleep(httpRetryWaitBeforeRetry);
                }
            }
        }

        // we shouldn't be here at all, but if we are, let's throw an Exception
        throw new RuntimeException("The retry method ended abnormally");
    }
}
