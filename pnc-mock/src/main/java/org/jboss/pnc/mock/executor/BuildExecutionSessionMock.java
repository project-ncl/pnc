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

package org.jboss.pnc.mock.executor;

import org.jboss.pnc.enums.BuildExecutionStatus;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.DebugData;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.jboss.pnc.spi.environment.EnvironmentDriverResult;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.events.BuildExecutionStatusChangedEvent;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;

import static org.jboss.pnc.enums.BuildExecutionStatus.DONE_WITH_ERRORS;

/**
 * This is a Copy of org.jboss.pnc.executor.DefaultBuildExecutionSession due to a module dependency issue.
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildExecutionSessionMock implements BuildExecutionSession {
    private static final Logger log = LoggerFactory.getLogger(BuildExecutionSessionMock.class);

    private final BuildExecutionConfiguration buildExecutionConfiguration;
    private final Consumer<BuildExecutionStatusChangedEvent> onBuildExecutionStatusChangedEvent;
    private BuildExecutionStatus status;
    private ExecutorException executorException;
    private Optional<URI> liveLogsUri;
    private Date startTime;
    private RunningEnvironment runningEnvironment;
    private Date endTime;
    private BuildDriverResult buildDriverResult;
    private RepositoryManagerResult repositoryManagerResult;
    // keep record of first received failed status
    private BuildExecutionStatus failedReasonStatus;

    private boolean cancelRequested = false;

    private Runnable cancelHook;

    private String accessToken;

    public BuildExecutionSessionMock(
            BuildExecutionConfiguration buildExecutionConfiguration,
            Consumer<BuildExecutionStatusChangedEvent> onBuildExecutionStatusChangedEvent) {
        liveLogsUri = Optional.empty();
        this.buildExecutionConfiguration = buildExecutionConfiguration;
        this.onBuildExecutionStatusChangedEvent = onBuildExecutionStatusChangedEvent;
    }

    @Override
    public Optional<URI> getLiveLogsUri() {
        return liveLogsUri;
    }

    @Override
    public void setLiveLogsUri(Optional<URI> liveLogsUri) {
        this.liveLogsUri = liveLogsUri;
    }

    @Override
    public void getEventLog() {

    }

    @Override
    public BuildExecutionConfiguration getBuildExecutionConfiguration() {
        return buildExecutionConfiguration;
    }

    @Override
    public BuildExecutionStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(BuildExecutionStatus status) {
        if (status.hasFailed() && failedReasonStatus == null) {
            if (status.equals(DONE_WITH_ERRORS)) {
                setException(
                        new ExecutorException(
                                "Missing failedReasonStatus. Failed reason must be sat before final DONE_WITH_ERRORS."));
            }
            log.debug("Setting status {} as failed reason for session {}.", status, getId());
            failedReasonStatus = status;
        }

        Optional<BuildResult> buildResult;
        if (status.isCompleted()) {
            buildResult = Optional.of(getBuildResult());
        } else {
            buildResult = Optional.empty();
        }
        BuildExecutionStatusChangedEvent statusChanged = new BuildExecutorStatusChangedEventMock(
                this.status,
                status,
                getId(),
                null,
                buildResult,
                status.isCompleted());

        log.debug("Updating build execution task {} status to {}.", getId(), statusChanged);
        this.status = status;
        onBuildExecutionStatusChangedEvent.accept(statusChanged);
        log.debug("Fired events after build execution task {} update.", getId());
    }

    private BuildResult getBuildResult() {
        EnvironmentDriverResult environmentDriverResult = null;
        DebugData debugData = getRunningEnvironment() != null ? getRunningEnvironment().getDebugData() : null;
        if (debugData != null && debugData.isDebugEnabled()) {
            environmentDriverResult = new EnvironmentDriverResult(
                    CompletionStatus.SUCCESS,
                    Optional.of(debugData.getSshCredentials()));
        }

        CompletionStatus completionStatus = CompletionStatus.SUCCESS;
        if (executorException == null) {
            if (failedReasonStatus != null) {
                switch (failedReasonStatus) {
                    case BUILD_ENV_SETUP_COMPLETE_WITH_ERROR:
                    case COLLECTING_RESULTS_FROM_REPOSITORY_MANAGER_COMPLETED_WITH_ERROR:
                    case SYSTEM_ERROR:
                        completionStatus = CompletionStatus.SYSTEM_ERROR;
                        break;

                    case BUILD_COMPLETED_WITH_ERROR:
                        completionStatus = CompletionStatus.FAILED;
                        break;

                    case CANCELLED:
                        completionStatus = CompletionStatus.CANCELLED;
                        break;

                    case DONE_WITH_ERRORS:
                        executorException = new ExecutorException("DONE_WITH_ERRORS cannot be set as failed reason.");
                        break;
                }
            }
        }

        ProcessException processException = null;
        if (executorException != null) {
            processException = new ProcessException(executorException);
            completionStatus = CompletionStatus.SYSTEM_ERROR;
        }

        log.debug("Returning result of task {}.", getId());

        return new BuildResult(
                completionStatus,
                Optional.ofNullable(processException),
                Optional.ofNullable(buildExecutionConfiguration),
                Optional.ofNullable(buildDriverResult),
                Optional.ofNullable(repositoryManagerResult),
                Optional.ofNullable(environmentDriverResult),
                Optional.empty());
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public ExecutorException getException() {
        return executorException;
    }

    @Override
    public void setException(ExecutorException executorException) {
        log.debug("Setting exception: {}", executorException != null ? executorException.getMessage() : "null");
        this.executorException = executorException;
    }

    @Override
    public Date getEndTime() {
        return endTime;
    }

    @Override
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Override
    public boolean hasFailed() {
        log.debug(
                "Has failed ? executorException: {} || failedReasonStatus: {}",
                executorException == null ? "" : executorException.getMessage(),
                failedReasonStatus);
        return executorException != null || failedReasonStatus != null;
    }

    @Override
    public String getId() {
        return getBuildExecutionConfiguration().getId();
    }

    @Override
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Override
    public RunningEnvironment getRunningEnvironment() {
        return runningEnvironment;
    }

    @Override
    public void setRunningEnvironment(RunningEnvironment runningEnvironment) {
        this.runningEnvironment = runningEnvironment;
    }

    @Override
    public void setBuildDriverResult(BuildDriverResult buildDriverResult) {
        this.buildDriverResult = buildDriverResult;
    }

    @Override
    public BuildDriverResult getBuildDriverResult() {
        return buildDriverResult;
    }

    @Override
    public void setRepositoryManagerResult(RepositoryManagerResult repositoryManagerResult) {
        this.repositoryManagerResult = repositoryManagerResult;
    }

    public synchronized void setCancelHook(Runnable cancelHook) {
        this.cancelHook = cancelHook;
    }

    public synchronized void cancel() {
        cancelRequested = true;
        if (cancelHook != null) {
            cancelHook.run();
        } else {
            log.warn("Trying to cancel operation while no cancel hook is defined.");
        }
    }

    public boolean isCanceled() {
        return cancelRequested;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
