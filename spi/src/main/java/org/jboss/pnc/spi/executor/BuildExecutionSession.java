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

package org.jboss.pnc.spi.executor;

import org.jboss.pnc.enums.BuildExecutionStatus;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;

import java.net.URI;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public interface BuildExecutionSession {
    Integer getId();

    Optional<URI> getLiveLogsUri();

    void setLiveLogsUri(Optional<URI> liveLogsUri);

    void getEventLog();

    BuildExecutionConfiguration getBuildExecutionConfiguration();

    BuildExecutionStatus getStatus();

    void setStatus(BuildExecutionStatus status);

    Date getStartTime();

    void setStartTime(Date date);

    ExecutorException getException();

    void setException(ExecutorException e);

    Date getEndTime();

    void setEndTime(Date date);

    boolean hasFailed();

    // BuildResult getBuildResult();

    RunningEnvironment getRunningEnvironment();

    void setRunningEnvironment(RunningEnvironment runningEnvironment);

    void setBuildDriverResult(BuildDriverResult buildDriverResult);

    BuildDriverResult getBuildDriverResult();

    void setRepositoryManagerResult(RepositoryManagerResult repositoryManagerResult);

    void setBuildStatusUpdateConsumer(
            Consumer<org.jboss.pnc.buildagent.api.TaskStatusUpdateEvent> clientStatusUpdateConsumer);

    Consumer<org.jboss.pnc.buildagent.api.TaskStatusUpdateEvent> getBuildStatusUpdateConsumer();

    String getAccessToken();

}
