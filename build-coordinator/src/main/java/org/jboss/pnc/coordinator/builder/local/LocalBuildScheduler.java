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

package org.jboss.pnc.coordinator.builder.local;

import org.jboss.pnc.common.util.TimeUtils;
import org.jboss.pnc.coordinator.builder.BuildScheduler;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.utils.ContentIdentityManager;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.events.BuildExecutionStatusChangedEvent;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.BuildExecutor;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
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

    @Override
    public String getId() {
        return ID;
    }

    @Deprecated
    public LocalBuildScheduler() {
    } // CDI workaround

    @Inject
    public LocalBuildScheduler(BuildExecutor buildExecutor) {
        this.buildExecutor = buildExecutor;
    }

    @Override
    public void startBuilding(BuildTask buildTask, Consumer<BuildResult> onComplete) throws CoreException {

        Consumer<BuildExecutionStatusChangedEvent> onBuildExecutionStatusChangedEvent = (statusChangedEvent) -> {
            log.debug("Received execution status update {}.", statusChangedEvent);
            if (statusChangedEvent.getNewStatus().isCompleted()) {
                BuildResult buildResult = statusChangedEvent.getBuildResult().get();
                log.debug("Notifying build execution completed {}.", statusChangedEvent);
                onComplete.accept(buildResult);
            }
        };

        String contentId = ContentIdentityManager.getBuildContentId(buildTask.getId());
        BuildConfigurationAudited configuration = buildTask.getBuildConfigurationAudited();
        BuildExecutionConfiguration buildExecutionConfiguration = BuildExecutionConfiguration.build(
                buildTask.getId(),
                contentId,
                buildTask.getUser().getId().toString(),
                configuration.getBuildScript(),
                configuration.getName(),
                configuration.getRepositoryConfiguration().getInternalUrl(),
                configuration.getScmRevision(),
                null,
                configuration.getRepositoryConfiguration().getExternalUrl(),
                configuration.getRepositoryConfiguration().isPreBuildSyncEnabled(),
                configuration.getBuildEnvironment().getSystemImageId(),
                configuration.getBuildEnvironment().getSystemImageRepositoryUrl(),
                configuration.getBuildEnvironment().getSystemImageType(),
                configuration.getBuildType(),
                buildTask.getBuildOptions().isKeepPodOnFailure(),
                configuration.getGenericParameters(),
                buildTask.getBuildOptions().isTemporaryBuild(),
                TimeUtils.generateTimestamp(
                        buildTask.getBuildOptions().isTimestampAlignment(),
                        buildTask.getBuildSetTask().getStartTime()),
                configuration.getDefaultAlignmentParams());

        try {
            buildExecutor.startBuilding(
                    buildExecutionConfiguration,
                    onBuildExecutionStatusChangedEvent,
                    buildTask.getUser().getLoginToken());
        } catch (ExecutorException e) {
            throw new CoreException("Could not start build execution.", e);
        }
    }

    @Override
    public boolean cancel(BuildTask buildTask) throws CoreException {
        try {
            buildExecutor.cancel(buildTask.getId());
        } catch (ExecutorException e) {
            throw new CoreException("Cannot cancel buildTask " + buildTask.getId(), e);
        }
        return false;
    }

}
