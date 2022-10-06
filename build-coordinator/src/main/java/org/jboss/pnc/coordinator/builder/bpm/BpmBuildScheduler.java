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

import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.bpm.Connector;
import org.jboss.pnc.bpm.RestConnector;
import org.jboss.pnc.bpm.task.BpmBuildTask;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.spi.coordinator.BuildScheduler;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.exception.CoreException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

import static org.jboss.pnc.bpm.ConnectorSelector.useNewProcessForBuild;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class BpmBuildScheduler implements BuildScheduler {

    private BpmManager manager;

    private BpmModuleConfig bpmConfig;

    private GlobalModuleGroup globalConfig;

    private Connector restConnector;

    @Deprecated
    public BpmBuildScheduler() { // CDI workaround
    }

    @Inject
    public BpmBuildScheduler(BpmManager manager, BpmModuleConfig bpmConfig, GlobalModuleGroup globalConfig) {
        this.manager = manager;
        this.bpmConfig = bpmConfig;
        this.globalConfig = globalConfig;
    }

    @PostConstruct
    public void init() {
        restConnector = new RestConnector(bpmConfig);
    }

    @PreDestroy
    private void dispose() {
        restConnector.close();
    }

    @Override
    public void startBuilding(BuildTask buildTask) throws CoreException {
        try {
            Map<String, String> genericParameters = buildTask.getBuildConfigurationAudited().getGenericParameters();
            BpmBuildTask task = new BpmBuildTask(buildTask);
            if (useNewProcessForBuild(genericParameters, bpmConfig.isNewBpmForced())) {
                task.setGlobalConfig(globalConfig);
                task.setBpmConfig(bpmConfig);
                task.setJsonEncodedProcessParameters(false);
                restConnector.startProcess(
                        bpmConfig.getBpmNewBuildProcessName(),
                        task.getExtendedProcessParameters(),
                        buildTask.getId(),
                        task.getAccessToken());
            } else {
                manager.startTask(task);
            }
        } catch (Exception e) {
            throw new CoreException("Error while trying to startBuilding with BpmBuildScheduler.", e);
        }
    }

    @Override
    public boolean cancel(BuildTask buildTask) {
        Map<String, String> genericParameters = buildTask.getBuildConfigurationAudited().getGenericParameters();
        BpmBuildTask task = new BpmBuildTask(buildTask);
        if (useNewProcessForBuild(genericParameters, bpmConfig.isNewBpmForced())) {
            return restConnector.cancelByCorrelation(buildTask.getId(), task.getAccessToken());
        } else {
            Optional<BpmBuildTask> taskOptional = manager.getActiveTasks()
                    .stream()
                    .filter(bpmTask -> bpmTask instanceof BpmBuildTask)
                    .map(bpmTask -> (BpmBuildTask) bpmTask)
                    .filter(bpmTask -> bpmTask.getBuildTask().getId().equals(buildTask.getId()))
                    .findAny();
            if (taskOptional.isPresent()) {
                return manager.cancelTask(taskOptional.get());
            } else {
                return false;
            }
        }
    }
}
