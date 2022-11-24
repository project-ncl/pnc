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
package org.jboss.pnc.remotecoordinator.maintenance;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.pnc.auth.KeycloakServiceClient;
import org.jboss.pnc.common.concurrent.NamedThreadFactory;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

@Dependent
public class BlacklistAsyncInvoker {
    private Logger logger = LoggerFactory.getLogger(BlacklistAsyncInvoker.class);

    private static final String BLACKLIST_ENDPOINT = "/listings/blacklist/gav";

    private GlobalModuleGroup globalModuleGroupConfiguration;

    private KeycloakServiceClient keycloakServiceClient;

    private ExecutorService executorService;

    @Deprecated // CDI workaround
    public BlacklistAsyncInvoker() {
    }

    @Inject
    public BlacklistAsyncInvoker(
            GlobalModuleGroup globalModuleGroupConfiguration,
            KeycloakServiceClient keycloakServiceClient) {
        this.globalModuleGroupConfiguration = globalModuleGroupConfiguration;
        this.keycloakServiceClient = keycloakServiceClient;

        executorService = Executors
                .newSingleThreadExecutor(new NamedThreadFactory("build-coordinator.BlacklistAsyncInvoker"));
    }

    public void notifyBlacklistToDA(String jsonPayload) {
        if (jsonPayload != null && !jsonPayload.isEmpty()) {
            logger.debug("Sending blacklisting payload to DA: {}", jsonPayload);
            executorService.submit(() -> {
                try {
                    String authToken = keycloakServiceClient.getAuthToken();
                    HttpUtils.performHttpPostRequest(
                            globalModuleGroupConfiguration.getDaUrl() + BLACKLIST_ENDPOINT,
                            jsonPayload,
                            authToken);
                } catch (JsonProcessingException e) {
                    logger.error("Failed to perform blacklist or deletion notification in DA.", e);
                }
            });
        }
    }

}
