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

package org.jboss.pnc.coordinator.builder.bpm;

import org.jboss.pnc.auth.KeycloakServiceClient;
import org.jboss.pnc.bpm.Connector;
import org.jboss.pnc.bpm.task.BpmBuildTask;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.spi.coordinator.BuildScheduler;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.exception.CoreException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class BpmBuildScheduler implements BuildScheduler {

    private BpmModuleConfig bpmConfig;

    private GlobalModuleGroup globalConfig;

    private Connector connector;

    private KeycloakServiceClient keycloakServiceClient;

    @Deprecated
    public BpmBuildScheduler() { // CDI workaround
    }

    @Inject
    public BpmBuildScheduler(
            BpmModuleConfig bpmConfig,
            GlobalModuleGroup globalConfig,
            Connector connector,
            KeycloakServiceClient keycloakServiceClient) {
        this.bpmConfig = bpmConfig;
        this.globalConfig = globalConfig;
        this.connector = connector;
        this.keycloakServiceClient = keycloakServiceClient;
    }

    @Override
    public void startBuilding(BuildTask buildTask) throws CoreException {
        try {
            BpmBuildTask task = new BpmBuildTask(buildTask, globalConfig);
            connector.startProcess(
                    bpmConfig.getBpmNewBuildProcessName(),
                    Collections.singletonMap("processParameters", task.getProcessParameters()),
                    buildTask.getId(),
                    keycloakServiceClient.getAuthToken());
        } catch (Exception e) {
            throw new CoreException("Error while trying to startBuilding with BpmBuildScheduler.", e);
        }
    }

    @Override
    public boolean cancel(BuildTask buildTask) {
        return connector.cancelByCorrelation(buildTask.getId(), keycloakServiceClient.getAuthToken());
    }
}
