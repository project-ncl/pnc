/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.coordinator;

import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.jboss.pnc.common.util.TimeUtils;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.utils.ContentIdentityManager;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildScheduler;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.InMemory;
import org.jboss.pnc.spi.events.BuildExecutionStatusChangedEvent;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.BuildExecutor;
import org.jboss.pnc.spi.executor.DefaultBuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
@Alternative
public class LocalBuildScheduler implements BuildScheduler {

    private static final Logger log = LoggerFactory.getLogger(LocalBuildScheduler.class);

    protected BuildExecutor buildExecutor;

    @InMemory
    protected BuildCoordinator buildCoordinator;

    @Deprecated
    public LocalBuildScheduler() {
    } // CDI workaround

    @Inject
    public LocalBuildScheduler(BuildExecutor buildExecutor, BuildCoordinator buildCoordinator) {
        this.buildExecutor = buildExecutor;
        this.buildCoordinator = buildCoordinator;
    }

    @Override
    public void startBuilding(BuildTask buildTask) throws CoreException {

        Consumer<BuildExecutionStatusChangedEvent> onBuildExecutionStatusChangedEvent = (statusChangedEvent) -> {
            try {
                log.debug("Received execution status update {}.", statusChangedEvent);
                if (statusChangedEvent.getNewStatus().isCompleted()) {
                    BuildResult buildResult = statusChangedEvent.getBuildResult().get();
                    log.debug("Notifying build execution completed {}.", statusChangedEvent);
                    buildCoordinator.completeBuild(buildTask, buildResult);
                }
            } catch (Throwable t) {
                log.error("Failed to notify build completion.", t);
            }
        };

        String contentId = ContentIdentityManager.getBuildContentId(buildTask.getId());
        BuildConfigurationAudited configuration = buildTask.getBuildConfigurationAudited();
        BuildExecutionConfiguration buildExecutionConfiguration = new DefaultBuildExecutionConfiguration(
                buildTask.getId(),
                contentId,
                buildTask.getUser().getId().toString(),
                configuration.getBuildScript(),
                configuration.getId().toString(),
                configuration.getName(),
                configuration.getRepositoryConfiguration().getInternalUrl(),
                configuration.getScmRevision(),
                null,
                configuration.getScmRevision(),
                false,
                configuration.getRepositoryConfiguration().getExternalUrl(),
                configuration.getRepositoryConfiguration().isPreBuildSyncEnabled(),
                configuration.getBuildEnvironment().getSystemImageId(),
                configuration.getBuildEnvironment().getSystemImageRepositoryUrl(),
                configuration.getBuildEnvironment().getSystemImageType(),
                configuration.getBuildType(),
                buildTask.getBuildOptions().isKeepPodOnFailure(),
                null,
                configuration.getGenericParameters(),
                buildTask.getBuildOptions().isTemporaryBuild(),
                TimeUtils.generateTimestamp(
                        buildTask.getBuildOptions().isTimestampAlignment(),
                        buildTask.getStartTime()),
                configuration.isBrewPullActive(),
                configuration.getDefaultAlignmentParams(),
                buildTask.getBuildOptions().getAlignmentPreference(),
                buildTask.getBuildOptions().getRebuildMode());

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
    public void startBuilding(BuildSetTask buildSetTask) throws CoreException {
        for (BuildTask buildTask : buildSetTask.getBuildTasks()) {
            startBuilding(buildTask);
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
