/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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

package org.jboss.pnc.executor;

import org.jboss.pnc.spi.BuildExecutionStatus;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
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

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DefaultBuildExecutionSession implements BuildExecutionSession {

    private static final Logger log = LoggerFactory.getLogger(DefaultBuildExecutionSession.class);

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
    //keep record of first received failed status
    private BuildExecutionStatus failedReasonStatus;

    public DefaultBuildExecutionSession(BuildExecutionConfiguration buildExecutionConfiguration, Consumer<BuildExecutionStatusChangedEvent> onBuildExecutionStatusChangedEvent) {
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
            log.debug("Setting status {} as failed reason for session {}.", status, getId());
            failedReasonStatus = status;
        }

        Optional<BuildResult> buildResult;
        if (status.isCompleted()) {
            buildResult = Optional.of(getBuildResult());
        } else {
            buildResult = Optional.empty();
        }
        BuildExecutionStatusChangedEvent statusChanged = new DefaultBuildExecutorStatusChangedEvent(
                this.status,
                status,
                getId(),
                buildExecutionConfiguration.getId(),
                buildResult);

        log.debug("Updating build execution task {} status to {}. Task is linked to coordination task {}.", getId(), statusChanged, "//TODO"); //TODO update
        this.status = status;
        onBuildExecutionStatusChangedEvent.accept(statusChanged);
        log.debug("Fired events after build execution task {} update.", getId()); //TODO update
    }

    //    @Override
    private BuildResult getBuildResult() {
        if (executorException == null) {
            if (failedReasonStatus == null) {
                log.trace("Returning result of task {} with no exception.", getId());
                return new BuildResult(Optional.ofNullable(buildDriverResult), Optional.ofNullable(repositoryManagerResult), Optional.empty(), Optional.empty());
            } else {
                log.trace("Returning result of task " + getId() + " with failed reason {}.", failedReasonStatus);
                return new BuildResult(Optional.ofNullable(buildDriverResult), Optional.ofNullable(repositoryManagerResult), Optional.empty(), Optional.of(failedReasonStatus));
            }
        } else {
            log.trace("Returning result of task " + getId() + " with exception.", executorException);
            return new BuildResult(Optional.ofNullable(buildDriverResult), Optional.ofNullable(repositoryManagerResult), Optional.of(executorException), Optional.empty());
        }
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
        return executorException != null || failedReasonStatus != null;
    }

    @Override
    public Integer getId() {
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
}
