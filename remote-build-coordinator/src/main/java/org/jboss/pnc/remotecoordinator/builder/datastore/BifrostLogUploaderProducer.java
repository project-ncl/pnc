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
package org.jboss.pnc.remotecoordinator.builder.datastore;

import org.jboss.pnc.auth.KeycloakServiceClient;
import org.jboss.pnc.bifrost.upload.BifrostLogUploader;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.net.URI;

@ApplicationScoped
public class BifrostLogUploaderProducer {

    private final BifrostLogUploader logUploader;

    @Inject
    public BifrostLogUploaderProducer(
            GlobalModuleGroup globalConfig,
            SystemConfig systemConfig,
            KeycloakServiceClient serviceAccountClient) {
        logUploader = new BifrostLogUploader(
                URI.create(globalConfig.getExternalBifrostUrl()),
                () -> "Bearer " + serviceAccountClient.getAuthToken(),
                systemConfig.getBifrostLogUploadMaxRetries(),
                systemConfig.getBifrostLogUploadRetryDelay());
    }

    @Produces
    public BifrostLogUploader produce() {
        return logUploader;
    }
}
