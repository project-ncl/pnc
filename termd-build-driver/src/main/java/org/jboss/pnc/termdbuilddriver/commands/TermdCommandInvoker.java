/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.termdbuilddriver.commands;

import org.jboss.pnc.termdbuilddriver.statusupdates.TermdStatusUpdatesConnection;
import org.jboss.pnc.termdbuilddriver.statusupdates.event.Status;
import org.jboss.pnc.termdbuilddriver.statusupdates.event.UpdateEvent;
import org.jboss.pnc.termdbuilddriver.websockets.TermdTerminalConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

public class TermdCommandInvoker {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private TermdTerminalConnection termdTerminalConnection;
    private TermdStatusUpdatesConnection termdStatusUpdatesConnection;
    private URI baseServerUri;
    private Path workingDirectory;

    private BlockingQueue<UpdateEvent> eventQueue = new LinkedBlockingQueue<>();
    private Queue<InvocatedCommandResult> invokedCommands = new LinkedList<>();

    public TermdCommandInvoker(URI serverBaseUri, Path workingDirectory) {
        this.baseServerUri = serverBaseUri;
        this.termdTerminalConnection = new TermdTerminalConnection(serverBaseUri);
        this.termdStatusUpdatesConnection = new TermdStatusUpdatesConnection(serverBaseUri);
        this.workingDirectory = workingDirectory;
    }

    /**
     * Performs command and the invocation is successful - returns proper result.
     *
     * Otherwise a {@link org.jboss.pnc.termdbuilddriver.TermdException} or its subclasses will be thrown and should be handled
     * using {@link java.util.concurrent.CompletableFuture#exceptionally(java.util.function.Function)}.
     *
     * @param command Command to be executed.
     * @return Completable future representing the result.
     */
    public CompletableFuture<InvocatedCommandResult> performCommand(String command) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Performing command {}", command);
            try {
                String data = "{\"action\":\"read\",\"data\":\"" + command + "\\n\"}";
                termdTerminalConnection.sendAsBinary(ByteBuffer.wrap(data.getBytes()));
                String logsDirectory = getLogsDirectory();
                logger.debug("Taking event from queue...");
                UpdateEvent updateEvent = eventQueue.take(); //TODO remove blocking operation
                InvocatedCommandResult invocatedCommandResult = new InvocatedCommandResult(updateEvent, baseServerUri, logsDirectory);
                logger.debug("Received command result {}", invocatedCommandResult);
                invokedCommands.add(invocatedCommandResult);
                return invocatedCommandResult;
            } catch (Exception e) {
                throw new TermdCommandExecutionException("There was a problem when invoking command " + command, e);
            }
        });
    }

    protected String getLogsDirectory() {
        return workingDirectory.toAbsolutePath().toString();
    }

    public void startSession() {
        logger.debug("Starting command session");
        termdStatusUpdatesConnection.connect();
        termdTerminalConnection.connect();

        termdStatusUpdatesConnection.addUpdateConsumer(event -> {
            logger.debug("Received event {}.", event);
            if(event.getEvent().getOldStatus() == Status.RUNNING) {
                try {
                    logger.debug("Adding event to queue {}.", event);
                    eventQueue.put(event);
                } catch (InterruptedException e) {
                    throw new TermdCommandExecutionException("Interrupted while waiting for queue space", e);
                }
            }
        });
    }

    public TermdCommandBatchExecutionResult closeSession() {
        logger.debug("Closing command session");
        termdTerminalConnection.disconnect();
        termdStatusUpdatesConnection.disconnect();

        TermdCommandBatchExecutionResult returnedResults = new TermdCommandBatchExecutionResult(invokedCommands);
        invokedCommands.clear();
        return returnedResults;
    }

    public URI getLogsURI() {
        return termdTerminalConnection.getLogsURI();
    }

}
