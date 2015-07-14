/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.termdbuilddriver.websockets.TermdConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Path;

public class TermdFileTranser {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String UPLOAD_PATH = "/servlet/upload";

    private final URI baseServerUri;

    public TermdFileTranser(URI baseServerUri) {
        this.baseServerUri = baseServerUri;
    }

    public TermdFileTranser() {
        this.baseServerUri = null;
    }

    public StringBuilder downloadFileToStringBuilder(StringBuilder logsAggregate, URI uri) {
        try {
            logger.debug("Downloading file to String Buffer from {}", uri);

            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");

            connection.setDoOutput(true);
            connection.setDoInput(true);

            int contentLength = connection.getContentLength();

            byte[] receivedBytes = new byte[contentLength];
            try (InputStream inputStream = connection.getInputStream()) {
                inputStream.read(receivedBytes);
            }

            logsAggregate.append("==== ").append(uri.toString()).append(" ====\n");
            String downloadedText = new String(receivedBytes);
            logsAggregate.append(downloadedText);

            logger.debug("Downloaded {}", new String(receivedBytes));
            return logsAggregate;
        } catch (IOException e) {
            throw new TermdTransferException("Could not obtain log file: " + uri.toString(), e);
        }
    }

    public void uploadScript(StringBuilder script, Path remoteFilePath) {
        logger.debug("Uploading build script to remote path {}, build script {}", remoteFilePath, script.toString());

        URI uploadUri = baseServerUri.resolve(UPLOAD_PATH + remoteFilePath.toAbsolutePath().toString());
        try {
            HttpURLConnection connection = (HttpURLConnection) uploadUri.toURL().openConnection();
            connection.setRequestMethod("PUT");

            connection.setDoOutput(true);
            connection.setDoInput(true);

            String fileContent = script.toString();
            byte[] fileContentBytes = fileContent.getBytes();
            connection.setRequestProperty("Content-Length", "" + Integer.toString(fileContentBytes.length));

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(fileContentBytes);
            }

            if(200 != connection.getResponseCode()) {
                throw new TermdConnectionException("Could not upload script to Build Agent. Returned status code " + connection.getResponseCode());
            }
            logger.debug("Uploaded successfully");
        } catch (IOException e) {
            throw new TermdConnectionException("Could not upload build script: " + uploadUri.toString(), e);
        }
    }

}
