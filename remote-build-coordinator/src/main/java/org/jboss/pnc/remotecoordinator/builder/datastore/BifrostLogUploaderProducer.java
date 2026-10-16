/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.builder.datastore;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.pnc.auth.ServiceAccountClient;
import org.jboss.pnc.bifrost.upload.BifrostLogUploader;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;

@ApplicationScoped
public class BifrostLogUploaderProducer {

    private final BifrostLogUploader logUploader;

    @Inject
    public BifrostLogUploaderProducer(
            GlobalModuleGroup globalConfig,
            SystemConfig systemConfig,
            ServiceAccountClient serviceAccountClient) {
        logUploader = new BifrostLogUploader(
                URI.create(globalConfig.getExternalBifrostUrl()),
                serviceAccountClient::getAuthHeaderValue,
                systemConfig.getBifrostLogUploadMaxRetries(),
                systemConfig.getBifrostLogUploadRetryDelay());
    }

    @Produces
    public BifrostLogUploader produce() {
        return logUploader;
    }
}
