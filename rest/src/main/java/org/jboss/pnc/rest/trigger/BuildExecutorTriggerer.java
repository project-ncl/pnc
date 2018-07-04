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

package org.jboss.pnc.rest.trigger;

import org.jboss.logging.Logger;
import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.bpm.BpmTask;
import org.jboss.pnc.bpm.task.BpmBuildTask;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.mdc.MDCMeta;
import org.jboss.pnc.rest.executor.notifications.NotificationSender;
import org.jboss.pnc.rest.restmodel.bpm.BpmTaskStatus;
import org.jboss.pnc.rest.restmodel.bpm.ProcessProgressUpdate;
import org.jboss.pnc.rest.utils.BpmNotifier;
import org.jboss.pnc.spi.BuildExecutionStatus;
import org.jboss.pnc.spi.events.BuildExecutionStatusChangedEvent;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.jboss.pnc.spi.executor.BuildExecutor;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.net.URI;
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

    //TODO decouple executor
    @Deprecated
    BpmManager bpmManager;

    private SystemConfig systemConfig;

    private NotificationSender notificationSender;

    @Deprecated //CDI workaround
    public BuildExecutorTriggerer() {}

    @Inject
    public BuildExecutorTriggerer(
            BuildExecutor buildExecutor,
            BpmNotifier bpmNotifier,
            NotificationSender notificationSender,
            BpmManager bpmManager,
            SystemConfig systemConfig) {
        this.buildExecutor = buildExecutor;
        this.bpmNotifier = bpmNotifier;
        this.notificationSender = notificationSender;
        this.bpmManager = bpmManager;
        this.systemConfig = systemConfig;
    }

    public BuildExecutionSession executeBuild(
            BuildExecutionConfiguration buildExecutionConfig,
            String callbackUrl,
            String accessToken)
            throws CoreException, ExecutorException {

        Consumer<BuildExecutionStatusChangedEvent> onExecutionStatusChange = (statusChangedEvent) -> {
            log.debug("Received BuildExecutionStatusChangedEvent: " + statusChangedEvent);

            Optional<ProcessProgressUpdate> processProgressUpdate = toProcessProgressUpdate(statusChangedEvent);
            if (processProgressUpdate.isPresent()) {
                notificationSender.send(processProgressUpdate.get());
                //As there is a plan to split the Executor from Coordinator, the notification should be sent over http
                //to the endpoint /bpm/tasks/{taskId}/notify
                //bpmManager should be aupdated to accept notifications identified by buildTaskId
                Optional<BpmTask> bpmTask = BpmBuildTask.getBpmTaskByBuildTaskId(bpmManager,
                        statusChangedEvent.getBuildTaskId());
                if (bpmTask.isPresent()) {
                    bpmManager.notify(bpmTask.get().getTaskId(), processProgressUpdate.get());
                } else {
                    log.warn("There is no bpmTask for buildTask.id: " + statusChangedEvent.getBuildTaskId() + ". Skipping notification.");
                }

            }
            if (statusChangedEvent.isFinal() && callbackUrl != null && !callbackUrl.isEmpty()) {
                statusChangedEvent.getBuildResult().ifPresent((buildResult) -> {
                    bpmNotifier.sendBuildExecutionCompleted(callbackUrl.toString(), buildResult);
                });
            }
        };
        BuildExecutionSession buildExecutionSession =
                buildExecutor.startBuilding(buildExecutionConfig, onExecutionStatusChange, accessToken);

        return buildExecutionSession;
    }

    private Optional<ProcessProgressUpdate> toProcessProgressUpdate(BuildExecutionStatusChangedEvent statusChangedEvent) {
        BuildExecutionStatus status = statusChangedEvent.getNewStatus();

        String taskName = null;
        BpmTaskStatus bpmTaskStatus = BpmTaskStatus.STARTING;
        String wsDetails = "";

        switch (status) {
            case REPO_SETTING_UP:
                taskName = "Repository";
                break;

            case BUILD_ENV_SETTING_UP:
                taskName = "Environment";
                break;

            case BUILD_WAITING:
                taskName = "Build";
                bpmTaskStatus = BpmTaskStatus.STARTED;
                BuildExecutionSession runningExecution = buildExecutor.getRunningExecution(statusChangedEvent.getBuildTaskId());
                Optional<URI> liveLogsUri = runningExecution.getLiveLogsUri();
                if (liveLogsUri.isPresent()) {
                    wsDetails = liveLogsUri.get().toString();
                } else {
                    log.warn("Missing live log url for buildExecution: " + statusChangedEvent.getBuildTaskId());
                }
                break;

            case COLLECTING_RESULTS_FROM_BUILD_DRIVER:
                taskName = "Collecting results from build";
                break;

            case COLLECTING_RESULTS_FROM_REPOSITORY_MANAGER:
                taskName = "Collecting results from repository";
                break;

            case BUILD_ENV_DESTROYING:
                taskName = "Destroying environment";
                break;

            case FINALIZING_EXECUTION:
                taskName = "Finalizing";
                break;
        }

        if (taskName != null) {
            return Optional.of(new ProcessProgressUpdate(taskName, bpmTaskStatus, wsDetails));
        } else {
            return Optional.empty();
        }
    }

    public void cancelBuild(Integer buildExecutionConfigId) throws CoreException, ExecutorException {
        buildExecutor.cancel(buildExecutionConfigId);
    }

    public Optional<MDCMeta> getMdcMeta(Integer buildExecutionConfigId) {
        BuildExecutionSession runningExecution = buildExecutor.getRunningExecution(buildExecutionConfigId);
        if (runningExecution != null) {
            BuildExecutionConfiguration buildExecutionConfiguration = runningExecution.getBuildExecutionConfiguration();
            return Optional.of(new MDCMeta(
                    buildExecutionConfiguration.getBuildContentId(),
                    buildExecutionConfiguration.isTempBuild(),
                    systemConfig.getTemporalBuildExpireDate()
            ));
        } else {
            return Optional.empty();
        }
    }
}
