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
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
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
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.jboss.pnc.test.util.Wait;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static org.jboss.pnc.enums.BuildExecutionStatus.BUILD_ENV_SETTING_UP;
import static org.jboss.pnc.enums.BuildExecutionStatus.BUILD_ENV_SETUP_COMPLETE_SUCCESS;
import static org.jboss.pnc.enums.BuildExecutionStatus.BUILD_ENV_SETUP_COMPLETE_WITH_ERROR;
import static org.jboss.pnc.enums.BuildExecutionStatus.BUILD_ENV_WAITING;
import static org.jboss.pnc.enums.BuildExecutionStatus.BUILD_SETTING_UP;
import static org.jboss.pnc.enums.BuildExecutionStatus.DONE_WITH_ERRORS;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunWith(Arquillian.class)
public class BuildEnvironmentTest {

    private static final Logger log = LoggerFactory.getLogger(BuildEnvironmentTest.class);

    @Inject
    TestProjectConfigurationBuilder configurationBuilder;

    @Deployment
    public static JavaArchive createDeployment() {
        return BuildExecutorDeployments
                .deployment(BuildExecutorDeployments.Options.ENV_DRIVER_WITH_FAILED_CONTAINER_INITIALIZATION);
    }

    @Inject
    RepositoryManagerFactory repositoryManagerFactory;

    @Inject
    EnvironmentDriverFactory environmentDriverFactory;

    @Inject
    BuildDriverFactory buildDriverFactory;

    private void checkBuildStatuses(
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

    @Test
    public void shouldReportErrorInCaseOfPodCreationFailure()
            throws ExecutorException, InterruptedException, TimeoutException {
        BuildConfiguration buildConfiguration = configurationBuilder.build(123, "Pod creation failure");

        Set<BuildExecutionStatusChangedEvent> statusChangedEvents = new HashSet<>();
        ObjectWrapper<BuildResult> buildExecutionResultWrapper = new ObjectWrapper<>();

        runBuild(buildConfiguration, statusChangedEvents, buildExecutionResultWrapper, true);

        checkBuildStatuses(
                statusChangedEvents,
                Arrays.asList(
                        BUILD_ENV_SETTING_UP,
                        BUILD_ENV_WAITING,
                        BUILD_ENV_SETUP_COMPLETE_WITH_ERROR,
                        // SYSTEM_ERROR, //TODO should be system error instead of DONE_WITH_ERRORS but it is not
                        // supported yet
                        DONE_WITH_ERRORS));
        assertNoState(statusChangedEvents, BUILD_ENV_SETUP_COMPLETE_SUCCESS);
        assertNoState(statusChangedEvents, BUILD_SETTING_UP);
    }

    private void assertNoState(Set<BuildExecutionStatusChangedEvent> statusEvents, BuildExecutionStatus state) {
        Assertions.assertThat(statusEvents.stream().anyMatch(e -> e.getNewStatus() == state)).isFalse();
    }

    private void runBuild(
            BuildConfiguration buildConfiguration,
            Set<BuildExecutionStatusChangedEvent> statusChangedEvents,
            ObjectWrapper<BuildResult> buildExecutionResultWrapper,
            boolean keepAliveOnFailure) throws ExecutorException {
        DefaultBuildExecutor executor = null;
        try {
            executor = new DefaultBuildExecutor(
                    repositoryManagerFactory,
                    buildDriverFactory,
                    environmentDriverFactory,
                    new Configuration(),
                    null);
        } catch (ConfigurationParseException e) {
            log.error(e.toString());
        }

        Consumer<BuildExecutionStatusChangedEvent> onBuildExecutionStatusChangedEvent = (statusChangedEvent) -> {
            log.debug("Received execution status update {}.", statusChangedEvent);
            statusChangedEvents.add(statusChangedEvent);

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
                keepAliveOnFailure,
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
}
