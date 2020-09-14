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
package org.jboss.pnc.termdbuilddriver;

import org.jboss.pnc.buildagent.api.Status;
import org.jboss.pnc.buildagent.api.TaskStatusUpdateEvent;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.enums.BuildStatus.CANCELLED;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class TermdBuildDriverTest extends AbstractLocalBuildAgentTest {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    SystemConfig systemConfig = mock(SystemConfig.class);

    @Test(timeout = 15_000)
    public void shouldFetchFromGitAndBuild() throws Throwable {
        // given
        Path tmpRepo = Files.createTempDirectory("tmpRepo");
        String repoPath = "file://" + tmpRepo.toAbsolutePath().toString() + "/test-repo";
        ZipUtils.unzipToDir(tmpRepo, "/repo.zip");
        String dirName = "test-repo-cloned";

        TermdBuildDriver driver = new TermdBuildDriver(
                systemConfig,
                buildDriverModuleConfig,
                new DefaultClientFactory());
        BuildExecutionSession buildExecution = mock(BuildExecutionSession.class);
        BuildExecutionConfiguration buildExecutionConfiguration = mock(BuildExecutionConfiguration.class);
        doReturn(repoPath).when(buildExecutionConfiguration).getScmRepoURL();
        doReturn("master").when(buildExecutionConfiguration).getScmRevision();
        doReturn("mvn validate").when(buildExecutionConfiguration).getBuildScript();
        doReturn(dirName).when(buildExecutionConfiguration).getName();
        doReturn(buildExecutionConfiguration).when(buildExecution).getBuildExecutionConfiguration();

        doReturn(mock(RunningEnvironment.class)).when(buildExecution).getRunningEnvironment();

        AtomicReference<CompletedBuild> buildResult = new AtomicReference<>();

        CountDownLatch latch = new CountDownLatch(1);
        Consumer<CompletedBuild> onComplete = (completedBuild) -> {
            logger.info("Build completed.");
            buildResult.set(completedBuild);
            latch.countDown();
        };
        Consumer<Throwable> onError = (throwable) -> {
            logger.error("Error received: ", throwable);
            fail(throwable.getMessage());
        };

        // when
        RunningBuild runningBuild = driver
                .startProjectBuild(buildExecution, localEnvironmentPointer, onComplete, onError); // TODO set monitor
                                                                                                  // before the build
                                                                                                  // starts

        logger.info("Waiting for build to complete...");
        latch.await();
        // then
        assertThat(buildResult.get().getBuildResult()).isNotNull();
        assertThat(buildResult.get().getBuildResult().getBuildLog()).isNotEmpty();
        assertThat(Files.exists(localEnvironmentPointer.getWorkingDirectory())).isTrue();
        assertThat(Files.exists(localEnvironmentPointer.getWorkingDirectory().resolve(dirName))).isTrue();
    }

    @Test(timeout = 5_000)
    public void shouldStartAndCancelTheExecutionImmediately()
            throws ConfigurationParseException, BuildDriverException, InterruptedException, IOException {
        // given
        String dirName = "test-workdir";
        String logStart = "Running the command...";
        String logEnd = "Command completed.";

        TermdBuildDriver driver = new TermdBuildDriver(
                systemConfig,
                buildDriverModuleConfig,
                new DefaultClientFactory());
        BuildExecutionSession buildExecution = mock(BuildExecutionSession.class);
        BuildExecutionConfiguration buildExecutionConfiguration = mock(BuildExecutionConfiguration.class);
        doReturn("echo \"" + logStart + "\"; mvn validate; echo \"" + logEnd + "\";").when(buildExecutionConfiguration)
                .getBuildScript();
        doReturn(dirName).when(buildExecutionConfiguration).getName();
        doReturn(buildExecutionConfiguration).when(buildExecution).getBuildExecutionConfiguration();

        AtomicReference<CompletedBuild> buildResult = new AtomicReference<>();

        // when
        CountDownLatch latch = new CountDownLatch(1);
        Consumer<CompletedBuild> onComplete = (completedBuild) -> {
            buildResult.set(completedBuild);
            latch.countDown();
        };
        Consumer<Throwable> onError = (throwable) -> {
            logger.error("Error received: ", throwable);
            fail(throwable.getMessage());
        };
        RunningBuild runningBuild = driver
                .startProjectBuild(buildExecution, localEnvironmentPointer, onComplete, onError);
        runningBuild.cancel();

        latch.await();

        // then
        assertThat(buildResult.get().getBuildResult().getBuildLog()).doesNotContain(logEnd);
        assertThat(buildResult.get().getBuildResult().getBuildStatus()).isEqualTo(CANCELLED);
    }

    @Test(timeout = 5_000)
    public void shouldStartAndCancelWhileExecutingCommand()
            throws ConfigurationParseException, BuildDriverException, InterruptedException {
        // given
        String dirName = "test-workdir";
        String logStart = "Running the command...";
        String logEnd = "Command completed.";

        CountDownLatch latchCompleted = new CountDownLatch(1);

        ClientMockFactory mockFactory = new ClientMockFactory();
        TermdBuildDriver driver = new TermdBuildDriver(systemConfig, buildDriverModuleConfig, mockFactory);
        BuildExecutionSession buildExecution = mock(BuildExecutionSession.class);
        BuildExecutionConfiguration buildExecutionConfiguration = mock(BuildExecutionConfiguration.class);
        doReturn("echo \"" + logStart + "\"; mvn validate; echo \"" + logEnd + "\";").when(buildExecutionConfiguration)
                .getBuildScript();
        doReturn(dirName).when(buildExecutionConfiguration).getName();
        doReturn(buildExecutionConfiguration).when(buildExecution).getBuildExecutionConfiguration();

        AtomicReference<CompletedBuild> buildResult = new AtomicReference<>();

        // when
        Consumer<CompletedBuild> onComplete = (completedBuild) -> {
            buildResult.set(completedBuild);
            latchCompleted.countDown();
        };
        Consumer<Throwable> onError = (throwable) -> {
            logger.error("Error received: ", throwable);
            fail(throwable.getMessage());
        };
        RunningBuild runningBuild = driver
                .startProjectBuild(buildExecution, localEnvironmentPointer, onComplete, onError);
        runningBuild.cancel();
        // simulate update for "CTRL+C" on a command, which results in the command failing
        mockFactory.getOnStatusUpdate().accept(TaskStatusUpdateEvent.newBuilder().newStatus(Status.FAILED).build());
        latchCompleted.await();

        // then
        assertThat(buildResult.get().getBuildResult()).isNotNull();
        assertThat(buildResult.get().getBuildResult().getBuildStatus()).isEqualTo(CANCELLED);
    }
}
