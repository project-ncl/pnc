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

package org.jboss.pnc.core.builder.coordinator.local;

import org.jboss.pnc.core.builder.coordinator.BuildScheduler;
import org.jboss.pnc.core.builder.coordinator.BuildTask;
import org.jboss.pnc.core.content.ContentIdentityManager;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildExecutionStatusChangedEvent;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.BuildExecutionResult;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.jboss.pnc.spi.executor.BuildExecutor;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class LocalBuildScheduler implements BuildScheduler {

    private static final Logger log = LoggerFactory.getLogger(LocalBuildScheduler.class);

    public static final String ID = "local-build-scheduler";

    private BuildExecutor buildExecutor;
    private Event<BuildCoordinationStatusChangedEvent> buildStatusChangedEventNotifier;

    @Override
    public String getId() {
        return ID;
    }

    @Deprecated
    public LocalBuildScheduler() {} //CDI workaround

    @Inject
    public LocalBuildScheduler(BuildExecutor buildExecutor, Event<BuildCoordinationStatusChangedEvent> buildStatusChangedEventNotifier) {
        this.buildExecutor = buildExecutor;
        this.buildStatusChangedEventNotifier = buildStatusChangedEventNotifier;
    }

    @Override
    public void startBuilding(BuildTask buildTask, Consumer<BuildExecutionResult> onComplete)
            throws CoreException, ExecutorException {

        Consumer<BuildExecutionStatusChangedEvent> onBuildExecutionStatusChangedEvent = (statusChangedEvent) -> {
            log.debug("Received execution status update {}.", statusChangedEvent);
            if (statusChangedEvent.getNewStatus().isCompleted()) {
                BuildExecutionSession buildExecutionSession = statusChangedEvent.getBuildExecutionSession();

                BuildExecutionResult buildExecutionResult = BuildExecutionResult.build(
                        buildExecutionSession.hasFailed(),
                        buildExecutionSession.getBuildResult()
                );
                log.debug("Notifying build execution completed {}.", statusChangedEvent);
                onComplete.accept(buildExecutionResult);
            }
        };

        String contentId = ContentIdentityManager.getBuildContentId(buildTask.getBuildConfiguration().getName());
        BuildExecutionConfiguration buildExecutionConfiguration = BuildExecutionConfiguration.build(
                buildTask.getId(),
                buildTask.getBuildConfiguration(),
                buildTask.getBuildConfigurationAudited(),
                contentId,
                buildTask.getBuildConfiguration().getProject().getName(),
                buildTask.getUser()
        );

        buildExecutor.startBuilding(buildExecutionConfiguration, onBuildExecutionStatusChangedEvent);
    }
}
