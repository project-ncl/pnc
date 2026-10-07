/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.executor;

import java.net.URI;
import java.util.Date;
import java.util.Optional;

import org.jboss.pnc.enums.BuildExecutionStatus;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public interface BuildExecutionSession {
    String getId();

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

    String getAccessToken();

}
