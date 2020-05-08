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

package org.jboss.pnc.rest.trigger;

import org.jboss.logging.Logger;
import org.jboss.pnc.common.Date.ExpiresDate;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.logging.BuildTaskContext;
import org.jboss.pnc.bpm.notification.BpmNotifier;
import org.jboss.pnc.spi.events.BuildExecutionStatusChangedEvent;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.jboss.pnc.spi.executor.BuildExecutor;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Dependent
public class BuildExecutorTriggerer {

    private final Logger log = Logger.getLogger(BuildExecutorTriggerer.class);

    private BuildExecutor buildExecutor;

    private BpmNotifier bpmNotifier;

    private SystemConfig systemConfig;

    @Deprecated // CDI workaround
    public BuildExecutorTriggerer() {
    }

    @Inject
    public BuildExecutorTriggerer(BuildExecutor buildExecutor, BpmNotifier bpmNotifier, SystemConfig systemConfig) {
        this.buildExecutor = buildExecutor;
        this.bpmNotifier = bpmNotifier;
        this.systemConfig = systemConfig;
    }

    @SuppressWarnings("StringOperationCanBeSimplified")
    public BuildExecutionSession executeBuild(
            BuildExecutionConfiguration buildExecutionConfig,
            String callbackUrl,
            String accessToken) throws CoreException, ExecutorException {

        Consumer<BuildExecutionStatusChangedEvent> onExecutionStatusChange = (statusChangedEvent) -> {
            log.debug("Received BuildExecutionStatusChangedEvent: " + statusChangedEvent);
            if (statusChangedEvent.isFinal() && callbackUrl != null && !callbackUrl.isEmpty()) {
                statusChangedEvent.getBuildResult().ifPresent((buildResult) -> {
                    bpmNotifier.sendBuildExecutionCompleted(callbackUrl, buildResult);
                });
            }
        };
        BuildExecutionSession buildExecutionSession = buildExecutor
                .startBuilding(buildExecutionConfig, onExecutionStatusChange, accessToken);

        return buildExecutionSession;
    }

    public void cancelBuild(Integer buildExecutionConfigId) throws CoreException, ExecutorException {
        buildExecutor.cancel(buildExecutionConfigId);
    }

    public Optional<BuildTaskContext> getMdcMeta(Integer buildExecutionConfigId) {
        BuildExecutionSession runningExecution = buildExecutor.getRunningExecution(buildExecutionConfigId);
        if (runningExecution != null) {
            BuildExecutionConfiguration buildExecutionConfiguration = runningExecution.getBuildExecutionConfiguration();
            boolean temporaryBuild = buildExecutionConfiguration.isTempBuild();
            return Optional.of(
                    new BuildTaskContext(
                            buildExecutionConfiguration.getBuildContentId(),
                            buildExecutionConfiguration.getUserId(),
                            temporaryBuild,
                            ExpiresDate.getTemporaryBuildExpireDate(
                                    systemConfig.getTemporaryBuildsLifeSpan(),
                                    temporaryBuild)));
        } else {
            return Optional.empty();
        }
    }
}
