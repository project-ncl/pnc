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

import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.util.ObjectWrapper;
import org.jboss.pnc.executor.servicefactories.BuildDriverFactory;
import org.jboss.pnc.executor.servicefactories.EnvironmentDriverFactory;
import org.jboss.pnc.executor.servicefactories.RepositoryManagerFactory;
import org.jboss.pnc.mock.model.builders.ArtifactBuilder;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.spi.BuildExecutionStatus;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.events.BuildExecutionStatusChangedEvent;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.test.util.Wait;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static org.jboss.pnc.spi.BuildExecutionStatus.BUILD_ENV_DESTROYED;
import static org.jboss.pnc.spi.BuildExecutionStatus.BUILD_ENV_DESTROYING;
import static org.jboss.pnc.spi.BuildExecutionStatus.DONE_WITH_ERRORS;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunWith(Arquillian.class)
public class BuildExecutionTest {

    private static final Logger log = LoggerFactory.getLogger(BuildExecutionTest.class);

    @Inject
    TestProjectConfigurationBuilder configurationBuilder;

    @Deployment
    public static JavaArchive createDeployment() {
        return BuildExecutorDeployments.deployment();
    }

    @Inject
    RepositoryManagerFactory repositoryManagerFactory;

    @Inject
    EnvironmentDriverFactory environmentDriverFactory;

    @Inject
    BuildDriverFactory buildDriverFactory;

    BuildExecutionStatus[] baseBuildStatuses = {
            BuildExecutionStatus.NEW,
            BuildExecutionStatus.BUILD_ENV_SETTING_UP,
            BuildExecutionStatus.BUILD_ENV_WAITING,
            BuildExecutionStatus.BUILD_ENV_SETUP_COMPLETE_SUCCESS,
            BuildExecutionStatus.REPO_SETTING_UP,
            BuildExecutionStatus.BUILD_SETTING_UP,
            BuildExecutionStatus.BUILD_WAITING,
            BUILD_ENV_DESTROYING,
            BUILD_ENV_DESTROYED,
            BuildExecutionStatus.FINALIZING_EXECUTION,
    };

    @Test
    public void testBuild() throws ExecutorException, TimeoutException, InterruptedException, BuildDriverException {
        BuildConfiguration buildConfiguration = configurationBuilder.build(1, "c1-java");
        Set<BuildExecutionStatusChangedEvent> statusChangedEvents = new HashSet<>();
        ObjectWrapper<BuildResult> buildExecutionResultWrapper = new ObjectWrapper<>();

        runBuild(buildConfiguration, statusChangedEvents, buildExecutionResultWrapper);

        List<BuildExecutionStatus> expectedStatuses = getBuildExecutionStatusesSuccess();

        //check build statuses
        checkBuildStatuses(statusChangedEvents, expectedStatuses);

        //check results
        BuildResult buildResult = buildExecutionResultWrapper.get();

        //check results: logs
        BuildDriverResult buildDriverResult = buildResult.getBuildDriverResult().get();
        String buildLog = buildDriverResult.getBuildLog();
        Assert.assertTrue("Invalid build log.", buildLog.contains("Finished: SUCCESS"));

        //check results: artifacts
        RepositoryManagerResult repositoryManagerResult = buildResult.getRepositoryManagerResult().get();
        Assert.assertTrue("Missing build artifacts.", repositoryManagerResult.getBuiltArtifacts().size() > 0);
        Assert.assertTrue("Missing build dependencies.", repositoryManagerResult.getDependencies().size() > 0);

        Artifact artifact = repositoryManagerResult.getBuiltArtifacts().iterator().next();
        Assert.assertTrue("Invalid built artifact in the result.", artifact.getIdentifier().startsWith(ArtifactBuilder.IDENTIFIER_PREFIX));

    }

    private List<BuildExecutionStatus> getBuildExecutionStatusesSuccess() {
        List<BuildExecutionStatus> expectedStatuses = getBuildExecutionStatusesBase();
        expectedStatuses.add(BuildExecutionStatus.DONE);
        expectedStatuses.add(BuildExecutionStatus.BUILD_COMPLETED_SUCCESS);
        return expectedStatuses;
    }

    private ArrayList<BuildExecutionStatus> getBuildExecutionStatusesBase() {
        return new ArrayList<>(Arrays.asList(baseBuildStatuses));
    }

    @Test
    public void buildShouldFail() throws ExecutorException, TimeoutException, InterruptedException, BuildDriverException {
        BuildConfiguration buildConfiguration = configurationBuilder.buildFailingConfiguration(2, "failed-build", null);
        Set<BuildExecutionStatusChangedEvent> statusChangedEvents = new HashSet<>();
        ObjectWrapper<BuildResult> buildExecutionResultWrapper = new ObjectWrapper<>();

        runBuild(buildConfiguration, statusChangedEvents, buildExecutionResultWrapper);

        List<BuildExecutionStatus> expectedStatuses = getBuildExecutionStatusesBase();
        expectedStatuses.add(DONE_WITH_ERRORS);

        //check build statuses
        checkBuildStatuses(statusChangedEvents, expectedStatuses);
    }

    private void checkBuildStatuses(Set<BuildExecutionStatusChangedEvent> statusChangedEvents, List<BuildExecutionStatus> expectedStatuses) {
        expectedStatuses.forEach(expectedStatus -> {
            try {
                Wait.forCondition(() -> contains(statusChangedEvents, expectedStatus), 1, ChronoUnit.SECONDS, "Did not receive expected status " + expectedStatus.toString());
            } catch (Exception e) {
                log.error("Error in tests execution.", e);
                Assert.fail(e.getMessage());
            }
        });
    }

    @Test
    public void shouldNotContinueBuildOnMavenError() throws ExecutorException, InterruptedException, TimeoutException {
        BuildConfiguration buildConfiguration = configurationBuilder.buildFailingConfiguration(3, "build-failed-on-maven", null);
        Set<BuildExecutionStatusChangedEvent> statusChangedEvents = new HashSet<>();
        ObjectWrapper<BuildResult> buildExecutionResultWrapper = new ObjectWrapper<>();

        runBuild(buildConfiguration, statusChangedEvents, buildExecutionResultWrapper);

        checkBuildStatuses(statusChangedEvents, Arrays.asList(DONE_WITH_ERRORS, BUILD_ENV_DESTROYED, BUILD_ENV_DESTROYING));

        assertNoState(statusChangedEvents, BuildExecutionStatus.COLLECTING_RESULTS_FROM_REPOSITORY_MANAGER);
    }

    private void assertNoState(Set<BuildExecutionStatusChangedEvent> statusEvents, BuildExecutionStatus state) {
        Assertions.assertThat(statusEvents.stream().anyMatch(e -> e.getNewStatus() == state)).isFalse();
    }

    private void runBuild(BuildConfiguration buildConfiguration,
                          Set<BuildExecutionStatusChangedEvent> statusChangedEvents,
                          ObjectWrapper<BuildResult> buildExecutionResultWrapper) throws ExecutorException {
        DefaultBuildExecutor executor = new DefaultBuildExecutor(
                repositoryManagerFactory,
                buildDriverFactory,
                environmentDriverFactory,
                new Configuration()
        );

        Consumer<BuildExecutionStatusChangedEvent> onBuildExecutionStatusChangedEvent = (statusChangedEvent) -> {
            log.debug("Received execution status update {}.", statusChangedEvent);
            statusChangedEvents.add(statusChangedEvent);

            if (statusChangedEvent.getNewStatus().isCompleted()) {
                BuildResult buildResult = statusChangedEvent.getBuildResult().get();
                if (buildResult.hasFailed()) {
                    log.error("Build execution failed.", buildResult.getException());
                }

                buildExecutionResultWrapper.set(buildResult);
            }
        };

        BuildExecutionConfiguration buildExecutionConfiguration = new DefaultBuildExecutionConfiguration(
                1,
                "build-content-id",
                1,
                buildConfiguration.getBuildScript(),
                buildConfiguration.getName(),
                buildConfiguration.getScmRepoURL(),
                buildConfiguration.getScmRevision(),
                buildConfiguration.getBuildEnvironment().getSystemImageId(),
                buildConfiguration.getBuildEnvironment().getSystemImageRepositoryUrl(),
                buildConfiguration.getBuildEnvironment().getSystemImageType(),
                false);

        executor.startBuilding(buildExecutionConfiguration, onBuildExecutionStatusChangedEvent);
    }

    private boolean contains(Set<BuildExecutionStatusChangedEvent> statusChangedEvents, BuildExecutionStatus status) {
        return statusChangedEvents.stream().anyMatch(event -> event.getNewStatus().equals(status));
    }
}
