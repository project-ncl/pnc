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
package org.jboss.pnc.restclient;

import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.Configuration;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.client.SCMRepositoryClient;
import org.jboss.pnc.dto.notification.RepositoryCreationFailure;
import org.jboss.pnc.dto.notification.SCMRepositoryCreationSuccess;
import org.jboss.pnc.dto.requests.CreateAndSyncSCMRequest;
import org.jboss.pnc.dto.response.RepositoryCreationResponse;
import org.jboss.pnc.restclient.websocket.VertxWebSocketClient;
import org.jboss.pnc.restclient.websocket.WebSocketClient;

import java.util.concurrent.CompletableFuture;

import static org.jboss.pnc.restclient.websocket.predicates.SCMRepositoryNotificationPredicates.withFailedTaskId;
import static org.jboss.pnc.restclient.websocket.predicates.SCMRepositoryNotificationPredicates.withSuccessTaskId;

public class AdvancedSCMRepositoryClient extends SCMRepositoryClient {

    public AdvancedSCMRepositoryClient(Configuration configuration) {
        super(configuration);
    }

    private CompletableFuture<RepositoryCreationFailure> waitForScmCreationFailure(
            WebSocketClient webSocketClient,
            String taskId) {
        return webSocketClient.catchRepositoryCreationFailure(withFailedTaskId(taskId));
    }

    private CompletableFuture<SCMRepositoryCreationSuccess> waitForScmCreationSuccess(
            WebSocketClient webSocketClient,
            String taskId) {

        return webSocketClient.catchSCMRepositoryCreationSuccess(withSuccessTaskId(taskId));
    }

    /**
     * If the scm repository is already created and present in the Orch database, a RemoteResourceException is thrown.
     * It's recommended that you check the presence of the repository before creating it.
     *
     * @param request scm to create
     * @return CompletableFuture of the result, whether it was successful or not
     * @throws RemoteResourceException Most likely thrown if repository already exists
     * @throws ClientException if the response from the server is strange: both task id and scm repository is null
     */
    public CompletableFuture<SCMCreationResult> createNewAndWait(CreateAndSyncSCMRequest request)
            throws RemoteResourceException, ClientException {

        WebSocketClient webSocketClient = new VertxWebSocketClient();
        webSocketClient.connect("ws://" + configuration.getHost() + "/pnc-rest-new/notifications").join();

        RepositoryCreationResponse response = super.createNew(request);

        if (response.getTaskId() == null) {

            if (response.getRepository() != null) {
                // if repository is internal, it'll get created immediately. Just wrap the results into the
                // CompletableFuture
                SCMRepositoryCreationSuccess dto = new SCMRepositoryCreationSuccess(response.getRepository(), null);
                return CompletableFuture.completedFuture(new SCMCreationResult(true, dto, null));
            } else {
                throw new ClientException(
                        "Something went wrong on creation of repository. task id and repository are both null");
            }
        }

        // if bpm task called, listen to either creation or failure events
        return CompletableFuture
                .anyOf(
                        waitForScmCreationFailure(webSocketClient, response.getTaskId().toString()),
                        waitForScmCreationSuccess(webSocketClient, response.getTaskId().toString()))
                .thenApply(a -> {
                    if (a instanceof SCMRepositoryCreationSuccess) {
                        return new SCMCreationResult(true, (SCMRepositoryCreationSuccess) a, null);
                    } else if (a instanceof RepositoryCreationFailure) {
                        return new SCMCreationResult(false, null, (RepositoryCreationFailure) a);
                    } else {
                        return new SCMCreationResult(false, null, null);
                    }
                })
                .whenCompleteAsync((x, y) -> webSocketClient.disconnect());
    }

    /**
     * DTO object to hold the final result of scm creation, whether it's successful or not
     */
    public static class SCMCreationResult {

        private final boolean success;
        private final SCMRepositoryCreationSuccess scmRepositoryCreationSuccess;
        private final RepositoryCreationFailure repositoryCreationFailure;

        public SCMCreationResult(
                boolean success,
                SCMRepositoryCreationSuccess scmRepositoryCreationSuccess,
                RepositoryCreationFailure repositoryCreationFailure) {

            this.success = success;
            this.scmRepositoryCreationSuccess = scmRepositoryCreationSuccess;
            this.repositoryCreationFailure = repositoryCreationFailure;
        }

        public boolean isSuccess() {
            return success;
        }

        public SCMRepositoryCreationSuccess getScmRepositoryCreationSuccess() {
            return scmRepositoryCreationSuccess;
        }

        public RepositoryCreationFailure getRepositoryCreationFailure() {
            return repositoryCreationFailure;
        }
    }
}
