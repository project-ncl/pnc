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

package org.jboss.pnc.executor;

import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildExecutionStatus;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.events.BuildExecutionStatusChangedEvent;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DefaultBuildExecutionSession implements BuildExecutionSession { //TODO implement me

    private static final Logger log = LoggerFactory.getLogger(DefaultBuildExecutionSession.class);

    private final BuildExecutionConfiguration buildExecutionConfiguration;
    private final Consumer<BuildExecutionStatusChangedEvent> onBuildExecutionStatusChangedEvent;
    private BuildExecutionStatus status;
    private boolean failed;
    private ExecutorException executorException;
    private Optional<URI> liveLogsUri;
    private BuildResult buildResult;

    public DefaultBuildExecutionSession(BuildExecutionConfiguration buildExecutionConfiguration, Consumer<BuildExecutionStatusChangedEvent> onBuildExecutionStatusChangedEvent) {
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
        BuildExecutionStatusChangedEvent statusChanged = new DefaultBuildExecutorStatusChangedEvent(
                this.status,
                status,
                getId(),
                buildExecutionConfiguration.getId(),
                getUserId(),
                this);

        log.debug("Updating build execution task {} status to {}. Task is linked to coordination task {}.", getId(), statusChanged, "//TODO"); //TODO update
        this.status = status;
        if (status.hasFailed()) {
            failed = true;
        }
        onBuildExecutionStatusChangedEvent.accept(statusChanged);
        log.debug("Fired events after build execution task {} update.", getId()); //TODO update
    }

    public Integer getUserId() {
        return null; //TODO
    }

    @Override
    public Date getStartTime() {
        return null;
    }

    @Override
    public void setException(ExecutorException executorException) {
        this.executorException = executorException;
        failed = true;
    }

    @Override
    public Date getEndTime() {
        return null;
    }

    @Override
    public void setEndTime(Date date) {

    }

    @Override
    public boolean hasFailed() {
        return failed;
    }

    @Override
    public void setBuildResult(BuildResult buildResult) {
        this.buildResult = buildResult;
    }

    @Override
    public BuildResult getBuildResult() {
        return buildResult;
    }

    @Override
    public Integer getId() {
        return null; //TODO
    }

    @Override
    public void setStartTime(Date date) {
        //TODO
    }

    @Override
    public RunningEnvironment getRunningEnvironment() {
        return null; //TODO
    }

    @Override
    public void setRunningEnvironment(RunningEnvironment runningEnvironment) {
        //TODO
    }
}
