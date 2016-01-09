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

package org.jboss.pnc.executor.buildExecutor;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.util.ObjectWrapper;
import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.executor.DefaultBuildExecutionConfiguration;
import org.jboss.pnc.executor.DefaultBuildExecutor;
import org.jboss.pnc.executor.executor.BuildExecutor;
import org.jboss.pnc.core.content.ContentIdentityManager;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.core.test.buildCoordinator.ProjectBuilder;
import org.jboss.pnc.core.test.buildCoordinator.event.TestCDIBuildStatusChangedReceiver;
import org.jboss.pnc.executor.mock.configurationBuilders.TestProjectConfigurationBuilder;
import org.jboss.pnc.core.test.mock.BuildDriverMock;
import org.jboss.pnc.core.test.mock.DatastoreMock;
import org.jboss.pnc.executor.servicefactories.BuildDriverFactory;
import org.jboss.pnc.executor.servicefactories.EnvironmentDriverFactory;
import org.jboss.pnc.executor.servicefactories.RepositoryManagerFactory;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.mock.MockUser;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.BuildExecutionStatus;
import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildExecutionStatusChangedEvent;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.jboss.pnc.test.util.Wait;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunWith(Arquillian.class)
public class BuildExecution {

    private static final Logger log = LoggerFactory.getLogger(BuildExecution.class);

    @Inject
    TestProjectConfigurationBuilder configurationBuilder;

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addClass(Configuration.class)
                .addClass(BuildEnvironment.Builder.class)
                .addClass(TestCDIBuildStatusChangedReceiver.class)
                .addPackages(true, BuildDriverFactory.class.getPackage(), BuildDriverMock.class.getPackage(),
                        ContentIdentityManager.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("META-INF/logging.properties");

        log.debug(jar.toString(true));
        return jar;
    }

    @Inject
    BuildExecutor buildExecutor;

    @Inject
    RepositoryManagerFactory repositoryManagerFactory;

    @Inject
    EnvironmentDriverFactory environmentDriverFactory;

    @Inject
    BuildDriverFactory buildDriverFactory;

    @Test
    public void testSingleBuild_() throws ExecutorException {
        Consumer<BuildCoordinationStatusChangedEvent> statusUpdateConsumer = (event) -> {};
        DefaultBuildExecutor executor = new DefaultBuildExecutor(
                repositoryManagerFactory,
                buildDriverFactory,
                environmentDriverFactory,
                statusUpdateConsumer
        );

        BuildExecutionConfiguration buildExecutionConfiguration = new DefaultBuildExecutionConfiguration(
                1,
                buildConfiguration,
                buildConfigurationAudited,
                "top-content-id",
                "build-content-id",
                MockUser.newTestUser(1),
                1);

        Consumer<BuildExecutionStatusChangedEvent> onStatusUpdate = (event) -> {
            event.getNewStatus();
            event.getBuildExecutionSession().getLiveLogsUri();
            event.getBuildExecutionSession().getEventLog();

            event.getNewStatus().equals(BuildExecutionStatus.DONE);
        };
        executor.startBuilding(buildExecutionConfiguration, onStatusUpdate);
    }

    @Test
    @InSequence(10)
    public void testSingleBuild() throws BuildConflictException, TimeoutException, InterruptedException {
        ObjectWrapper<Boolean> completed = new ObjectWrapper<>(false);
        Consumer<BuildCoordinationStatus> onComplete = (buildStatus) -> {
            completed.set(true);
        };
        BuildConfiguration buildConfiguration = configurationBuilder.build(1, "c1-java");
        BuildConfigurationAudited configurationAudited = BuildConfigurationAudited.Builder.newBuilder()
                .buildConfiguration(buildConfiguration)
                .rev(1)
                .build();

        buildExecutor.build(buildConfiguration, configurationAudited, MockUser.newTestUser(1), onComplete, null, null, 1, new Date());

        Wait.forCondition(() -> completed.get(), 1, ChronoUnit.SECONDS, "Did not received build complete.");
    }

    @Test
    @InSequence(20)
    public void checkDatabaseForResult() {
        List<BuildRecord> buildRecords = datastore.getBuildRecords();
        Assert.assertEquals("Wrong datastore results count.", 1, buildRecords.size());

        BuildRecord buildRecord = buildRecords.get(0);
        String buildLog = buildRecord.getBuildLog();
        Assert.assertTrue("Invalid build log.", buildLog.contains("Finished: SUCCESS"));

        ProjectBuilder.assertBuildArtifactsPresent(buildRecord.getBuiltArtifacts());
        ProjectBuilder.assertBuildArtifactsPresent(buildRecord.getDependencies());
    }

    private void assertAllStatusUpdateReceived(List<BuildCoordinationStatusChangedEvent> receivedStatuses, Integer buildTaskId) {
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.BUILD_ENV_SETTING_UP, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.BUILD_ENV_WAITING, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.BUILD_ENV_SETUP_COMPLETE_SUCCESS, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.REPO_SETTING_UP, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.BUILD_SETTING_UP, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.BUILD_WAITING, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.BUILD_COMPLETED_SUCCESS, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.BUILD_ENV_DESTROYING, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.BUILD_ENV_DESTROYED, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.STORING_RESULTS, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.DONE, buildTaskId);
    }

    private void assertAllStatusUpdateReceivedForFailedBuild(List<BuildCoordinationStatusChangedEvent> receivedStatuses, Integer buildTaskId) {
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.BUILD_ENV_SETTING_UP, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.BUILD_ENV_WAITING, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.BUILD_ENV_SETUP_COMPLETE_SUCCESS, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.REPO_SETTING_UP, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.BUILD_SETTING_UP, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.BUILD_WAITING, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.BUILD_COMPLETED_WITH_ERROR, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.BUILD_ENV_DESTROYING, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.BUILD_ENV_DESTROYED, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.STORING_RESULTS, buildTaskId);
        assertStatusUpdateReceived(receivedStatuses, BuildCoordinationStatus.DONE_WITH_ERRORS, buildTaskId);
    }
}
