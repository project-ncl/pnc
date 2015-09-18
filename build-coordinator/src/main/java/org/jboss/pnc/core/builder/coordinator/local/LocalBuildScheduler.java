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
import org.jboss.pnc.core.builder.executor.BuildExecutionTask;
import org.jboss.pnc.core.builder.executor.BuildExecutor;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class LocalBuildScheduler implements BuildScheduler {

    private BuildExecutor buildExecutor;
    private Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier;

    @Override
    public String getId() {
        return "local-build-scheduler";
    }

    @Deprecated
    public LocalBuildScheduler() {} //CDI workaround

    @Inject
    public LocalBuildScheduler(BuildExecutor buildExecutor, Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier) {
        this.buildExecutor = buildExecutor;
        this.buildStatusChangedEventNotifier = buildStatusChangedEventNotifier;
    }

    @Override
    public void startBuilding(BuildTask buildTask, Consumer<BuildStatus> onComplete) throws CoreException {
        BuildExecutionTask buildExecutionTask = BuildExecutionTask.build(
                buildTask.getId(),
                buildTask.getBuildConfiguration(),
                buildTask.getBuildConfigurationAudited(),
                buildTask.getUser(),
                buildTask.getBuildRecordSetIds(),
                buildTask.getBuildConfigSetRecordId(),
                Optional.of(buildStatusChangedEventNotifier),
                buildTask.getId(),
                buildTask.getSubmitTime()
        );
        buildExecutor.startBuilding(buildExecutionTask, onComplete);
    }
}
