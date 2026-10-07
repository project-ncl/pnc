/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.coordinator.maintenance;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.pnc.auth.ServiceAccountClient;
import org.jboss.pnc.common.concurrent.NamedThreadFactory;
import org.jboss.pnc.common.http.HttpUtils;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

@Dependent
public class BlacklistAsyncInvoker {
    private Logger logger = LoggerFactory.getLogger(BlacklistAsyncInvoker.class);

    private static final String BLACKLIST_ENDPOINT = "/listings/blacklist/gav";

    private GlobalModuleGroup globalModuleGroupConfiguration;

    private ServiceAccountClient serviceAccountClient;

    private ExecutorService executorService;

    @Deprecated // CDI workaround
    public BlacklistAsyncInvoker() {
    }

    @Inject
    public BlacklistAsyncInvoker(
            GlobalModuleGroup globalModuleGroupConfiguration,
            ServiceAccountClient serviceAccountClient) {
        this.globalModuleGroupConfiguration = globalModuleGroupConfiguration;
        this.serviceAccountClient = serviceAccountClient;

        executorService = Executors
                .newSingleThreadExecutor(new NamedThreadFactory("build-coordinator.BlacklistAsyncInvoker"));
    }

    public void notifyBlacklistToDA(String jsonPayload) {
        if (jsonPayload != null && !jsonPayload.isEmpty()) {
            logger.debug("Sending blacklisting payload to DA: {}", jsonPayload);
            executorService.submit(() -> {
                try {
                    HttpUtils.performHttpPostRequest(
                            globalModuleGroupConfiguration.getDaUrl() + BLACKLIST_ENDPOINT,
                            jsonPayload,
                            serviceAccountClient.getAuthHeaderValue());
                } catch (JsonProcessingException e) {
                    logger.error("Failed to perform blacklist or deletion notification in DA.", e);
                }
            });
        }
    }

}
