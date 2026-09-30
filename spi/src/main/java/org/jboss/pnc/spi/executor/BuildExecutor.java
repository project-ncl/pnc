/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.executor;

import java.util.function.Consumer;

import org.jboss.pnc.spi.events.BuildExecutionStatusChangedEvent;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public interface BuildExecutor {

    BuildExecutionSession startBuilding(
            BuildExecutionConfiguration buildExecutionConfiguration,
            Consumer<BuildExecutionStatusChangedEvent> onBuildExecutionStatusChangedEvent,
            String accessToken) throws ExecutorException;

    BuildExecutionSession getRunningExecution(String buildExecutionTaskId);

    void shutdown();

    void cancel(String executionConfigurationId) throws ExecutorException;

}
