/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.buildagent.api.Status;
import org.jboss.pnc.buildagent.client.BuildAgentClient;
import org.jboss.pnc.buildagent.client.BuildAgentClientException;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
class RemoteInvocation implements Cloneable {

    private static final Logger logger = LoggerFactory.getLogger(RemoteInvocation.class);

    private BuildAgentClient buildAgentClient;

    private String scriptPath;

    private final CompletableFuture<Status> completionNotifier = new CompletableFuture<>();

    private boolean canceled = false;

    void invoke() {
        String command = "sh " + scriptPath;
        if (buildAgentClient != null) {
            try {
                logger.info("Invoking remote command {}.", command);
                buildAgentClient.executeCommand(command);
                logger.debug("Remote command invoked.");
            } catch (BuildAgentClientException e) {
                throw new RuntimeException("Cannot execute remote command.", e);
            }
        } else {
            logger.warn("There is no buildAgentClient to invoke command: {}", command);
        }
    }

    void cancel(String buildName) {
        try {
            canceled = true;
            logger.info("Canceling running build {}.", buildName);
            buildAgentClient.execute('C' - 64); //send ctrl+C
        } catch (BuildAgentClientException e) {
            completionNotifier.completeExceptionally(new BuildDriverException("Cannot cancel remote script.", e));
        }
    }

    public void setBuildAgentClient(BuildAgentClient buildAgentClient) {
        this.buildAgentClient = buildAgentClient;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public void notifyCompleted(Status status) {
        completionNotifier.complete(status);
    }

    public CompletableFuture<Status> getCompletionNotifier() {
        return completionNotifier;
    }

    public BuildAgentClient getBuildAgentClient() {
        return buildAgentClient;
    }

    public void close() {
        if (buildAgentClient != null) {
            logger.debug("Closing build agent client.");
            try {
                buildAgentClient.close();
                buildAgentClient = null; //make sure there is no reference left
            } catch (IOException e) {
                logger.error("Cannot close buildAgentClient.", e);
            }
        } else {
            //cancel has been requested
            logger.debug("There is no buildAgentClient probably cancel has been requested.");
        }
    }

    public boolean isCanceled() {
        return canceled;
    }
}
