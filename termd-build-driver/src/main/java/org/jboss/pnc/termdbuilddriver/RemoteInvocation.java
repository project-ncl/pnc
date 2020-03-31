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
package org.jboss.pnc.termdbuilddriver;

import org.jboss.pnc.buildagent.api.Status;
import org.jboss.pnc.buildagent.api.TaskStatusUpdateEvent;
import org.jboss.pnc.buildagent.client.BuildAgentClient;
import org.jboss.pnc.buildagent.client.BuildAgentClientException;
import org.jboss.pnc.common.concurrent.MDCWrappers;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static org.jboss.pnc.buildagent.api.Status.FAILED;
import static org.jboss.pnc.buildagent.api.Status.INTERRUPTED;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
class RemoteInvocation implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(RemoteInvocation.class);

    private BuildAgentClient buildAgentClient;

    private String scriptPath;

    private final CompletableFuture<RemoteInvocationCompletion> completionNotifier = new CompletableFuture<>();

    private boolean canceled = false;

    private Set<Runnable> preCloseListeners = new HashSet<>();

    public RemoteInvocation(
            ClientFactory buildAgentClientFactory,
            String terminalUrl,
            Optional<Consumer<Status>> onStatusUpdate) throws BuildDriverException {

        Consumer<TaskStatusUpdateEvent> onStatusUpdateInternal = (event) -> {
            final org.jboss.pnc.buildagent.api.Status newStatus;
            if (isCanceled() && event.getNewStatus().equals(FAILED)) {
                newStatus = INTERRUPTED; //TODO fix returned status and remove this workaround
            } else {
                newStatus = event.getNewStatus();
            }
            logger.debug("Driver received new status update {}.", newStatus);
            onStatusUpdate.ifPresent(c -> c.accept(newStatus));
            if (newStatus.isFinal()) {
                completionNotifier.complete(new RemoteInvocationCompletion(newStatus, Optional.ofNullable(event.getOutputChecksum())));
            }
        };

        try {
            buildAgentClient = buildAgentClientFactory.createBuildAgentClient(terminalUrl, MDCWrappers.wrap(onStatusUpdateInternal));
        } catch (TimeoutException | BuildAgentClientException | InterruptedException e) {
            throw new BuildDriverException("Cannot create Build Agent Client.", e);
        }
    }

    void invoke() {
        String command = "sh " + scriptPath;
        if (buildAgentClient != null) {
            try {
                logger.info("Invoking remote command {}.", command);
                buildAgentClient.execute(command);
                logger.debug("Remote command invoked.");
            } catch (BuildAgentClientException e) {
                throw new RuntimeException("Cannot execute remote command.", e);
            }
        } else {
            logger.warn("There is no buildAgentClient to invoke command: {}", command);
        }
    }

    void cancel() {
        canceled = true;
        try {
            logger.info("Canceling running build.");
            buildAgentClient.cancel();
        } catch (BuildAgentClientException e) {
            completionNotifier.completeExceptionally(new BuildDriverException("Cannot cancel remote script.", e));
        }
    }

    public void enableSsh() {
        try {
            buildAgentClient.execute("/usr/local/bin/startSshd.sh");
        } catch (BuildAgentClientException e) {
            logger.error("Failed to enable ssh access", e);
        }
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public void notifyCompleted(RemoteInvocationCompletion remoteInvocationCompletion) {
        completionNotifier.complete(remoteInvocationCompletion);
    }

    public CompletableFuture<RemoteInvocationCompletion> getCompletionNotifier() {
        return completionNotifier;
    }

    @Override
    public void close() {
        try {
            preCloseListeners.forEach(Runnable::run);
        } catch (Exception e) { // make sure close is not interrupted
            logger.error("Error in pre-close operation.", e);
        }

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

    public void addPreClose(Runnable task) {
        preCloseListeners.add(task);
    }

    public boolean isAlive() {
        return buildAgentClient.isServerAlive();
    }
}
