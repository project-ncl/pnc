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

package org.jboss.pnc.executor;

import org.assertj.core.api.Assertions;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.util.ObjectWrapper;
import org.jboss.pnc.executor.servicefactories.BuildDriverFactory;
import org.jboss.pnc.executor.servicefactories.EnvironmentDriverFactory;
import org.jboss.pnc.executor.servicefactories.RepositoryManagerFactory;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.enums.BuildExecutionStatus;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.events.BuildExecutionStatusChangedEvent;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.BuildExecutor;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.jboss.pnc.test.util.Wait;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
class BuildExecutionBase {

    private static final Logger log = LoggerFactory.getLogger(BuildExecutionBase.class);

    @Inject
    TestProjectConfigurationBuilder configurationBuilder;

    @Inject
    RepositoryManagerFactory repositoryManagerFactory;

    @Inject
    EnvironmentDriverFactory environmentDriverFactory;

    @Inject
    BuildDriverFactory buildDriverFactory;

    @Inject
    Configuration configuration;

    BuildExecutionStatus[] baseBuildStatuses = { BuildExecutionStatus.NEW, BuildExecutionStatus.BUILD_ENV_SETTING_UP,
            BuildExecutionStatus.BUILD_ENV_WAITING, BuildExecutionStatus.BUILD_ENV_SETUP_COMPLETE_SUCCESS,
            BuildExecutionStatus.REPO_SETTING_UP, BuildExecutionStatus.BUILD_SETTING_UP,
            BuildExecutionStatus.BUILD_WAITING, BuildExecutionStatus.BUILD_ENV_DESTROYING,
            BuildExecutionStatus.BUILD_ENV_DESTROYED, BuildExecutionStatus.FINALIZING_EXECUTION, };

    protected List<BuildExecutionStatus> getBuildExecutionStatusesSuccess() {
        List<BuildExecutionStatus> expectedStatuses = getBuildExecutionStatusesBase();
        expectedStatuses.add(BuildExecutionStatus.DONE);
        expectedStatuses.add(BuildExecutionStatus.BUILD_COMPLETED_SUCCESS);
        return expectedStatuses;
    }

    protected ArrayList<BuildExecutionStatus> getBuildExecutionStatusesBase() {
        return new ArrayList<>(Arrays.asList(baseBuildStatuses));
    }

    protected void checkBuildStatuses(
            Set<BuildExecutionStatusChangedEvent> statusChangedEvents,
            List<BuildExecutionStatus> expectedStatuses) {
        expectedStatuses.forEach(expectedStatus -> {
            try {
                Wait.forCondition(
                        () -> contains(statusChangedEvents, expectedStatus),
                        1,
                        ChronoUnit.SECONDS,
                        "Did not receive expected status " + expectedStatus.toString());
            } catch (Exception e) {
                log.error("Error in tests execution.", e);
                Assert.fail(e.getMessage());
            }
        });
    }

    protected void assertNoState(Set<BuildExecutionStatusChangedEvent> statusEvents, BuildExecutionStatus state) {
        Assertions.assertThat(statusEvents.stream().anyMatch(e -> e.getNewStatus() == state)).isFalse();
    }

    protected void runBuild(
            BuildConfiguration buildConfiguration,
            Set<BuildExecutionStatusChangedEvent> statusChangedEvents,
            ObjectWrapper<BuildResult> buildExecutionResultWrapper) throws ExecutorException {

        DefaultBuildExecutor executor = new DefaultBuildExecutor(
                repositoryManagerFactory,
                buildDriverFactory,
                environmentDriverFactory,
                configuration,
                null);

        runBuild(buildConfiguration, statusChangedEvents, buildExecutionResultWrapper, (e) -> {}, executor);
    }

    protected void runBuild(
            BuildConfiguration buildConfiguration,
            Set<BuildExecutionStatusChangedEvent> statusChangedEvents,
            ObjectWrapper<BuildResult> buildExecutionResultWrapper,
            Consumer<BuildExecutionStatusChangedEvent> onStatusUpdate,
            BuildExecutor executor) throws ExecutorException {

        Consumer<BuildExecutionStatusChangedEvent> onBuildExecutionStatusChangedEvent = (statusChangedEvent) -> {
            log.debug("Received execution status update {}.", statusChangedEvent);
            statusChangedEvents.add(statusChangedEvent);

            onStatusUpdate.accept(statusChangedEvent);

            if (statusChangedEvent.getNewStatus().isCompleted()) {
                BuildResult buildResult = statusChangedEvent.getBuildResult().get();
                buildExecutionResultWrapper.set(buildResult);
            }
        };

        BuildExecutionConfiguration buildExecutionConfiguration = new DefaultBuildExecutionConfiguration(
                "1",
                "build-content-id",
                "1",
                buildConfiguration.getBuildScript(),
                buildConfiguration.getName(),
                buildConfiguration.getRepositoryConfiguration().getInternalUrl(),
                buildConfiguration.getScmRevision(),
                null,
                buildConfiguration.getRepositoryConfiguration().getExternalUrl(),
                buildConfiguration.getRepositoryConfiguration().isPreBuildSyncEnabled(),
                buildConfiguration.getBuildType(),
                buildConfiguration.getBuildEnvironment().getSystemImageId(),
                buildConfiguration.getBuildEnvironment().getSystemImageRepositoryUrl(),
                buildConfiguration.getBuildEnvironment().getSystemImageType(),
                false,
                null,
                buildConfiguration.getGenericParameters(),
                false,
                null,
                buildConfiguration.isBrewPullActive(),
                buildConfiguration.getDefaultAlignmentParams());

        executor.startBuilding(buildExecutionConfiguration, onBuildExecutionStatusChangedEvent, "");
    }

    private boolean contains(Set<BuildExecutionStatusChangedEvent> statusChangedEvents, BuildExecutionStatus status) {
        return statusChangedEvents.stream().anyMatch(event -> event.getNewStatus().equals(status));
    }

    protected void buildStatusesShouldNotContain(
            Set<BuildExecutionStatusChangedEvent> statusChangedEvents,
            List<BuildExecutionStatus> unexpectedStatuses) {

        List<BuildExecutionStatus> statusReceived = statusChangedEvents.stream()
                .map(BuildExecutionStatusChangedEvent::getNewStatus)
                .collect(Collectors.toList());

        for (BuildExecutionStatusChangedEvent statusChangedEvent : statusChangedEvents) {
            BuildExecutionStatus status = statusChangedEvent.getNewStatus();
            if (unexpectedStatuses.contains(status)) {
                log.info("Received statuses: {}", statusReceived);
                Assert.fail("Unexpected status [" + status + "] has been received.");
            }
        }
    }

}
