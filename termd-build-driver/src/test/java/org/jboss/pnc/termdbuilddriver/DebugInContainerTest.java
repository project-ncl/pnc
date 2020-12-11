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

import org.assertj.core.api.Assertions;
import org.jboss.pnc.buildagent.api.Status;
import org.jboss.pnc.buildagent.api.TaskStatusUpdateEvent;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleconfig.TermdBuildDriverModuleConfig;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.DebugData;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DebugInContainerTest {

    private Logger logger = LoggerFactory.getLogger(DebugInContainerTest.class);

    private String HOST = "localhost";

    SystemConfig systemConfig = mock(SystemConfig.class);

    @Test
    public void shouldEnableSshWhenBuildFails() throws InterruptedException, BuildDriverException {

        TermdBuildDriverModuleConfig buildDriverModuleConfig = mock(TermdBuildDriverModuleConfig.class);
        doReturn(1000L).when(buildDriverModuleConfig).getLivenessProbeFrequencyMillis();
        doReturn(5000L).when(buildDriverModuleConfig).getLivenessFailTimeoutMillis();
        doReturn(5000).when(buildDriverModuleConfig).getFileTransferReadTimeout();

        ClientMockFactory buildAgentClientFactory = new ClientMockFactory();
        TermdBuildDriver driver = new TermdBuildDriver(systemConfig, buildDriverModuleConfig, buildAgentClientFactory);

        BuildExecutionSession buildExecution = mock(BuildExecutionSession.class);
        BuildExecutionConfiguration buildExecutionConfiguration = mock(BuildExecutionConfiguration.class);
        doReturn(buildExecutionConfiguration).when(buildExecution).getBuildExecutionConfiguration();

        RunningEnvironment runningEnvironment = mock(RunningEnvironment.class);
        doReturn(Paths.get("")).when(runningEnvironment).getWorkingDirectory();
        doReturn(new DebugData(true)).when(runningEnvironment).getDebugData();
        doReturn("http://localhost/").when(runningEnvironment).getInternalBuildAgentUrl();
        doReturn(runningEnvironment).when(buildExecution).getRunningEnvironment();

        BlockingQueue<CompletedBuild> result = new ArrayBlockingQueue(1);

        Consumer<CompletedBuild> onComplete = (completedBuild) -> {
            try {
                result.put(completedBuild);
            } catch (InterruptedException e) {
                Assert.fail("Unable to consume build result.");
            }
        };
        Consumer<Throwable> onError = (throwable) -> Assert.fail("Build should fail without system error.");

        // when
        RunningBuild runningBuild = driver.startProjectBuild(buildExecution, runningEnvironment, onComplete, onError);
        Thread.sleep(500); // wait to start waiting for completion and start liveness probe
        buildAgentClientFactory.getOnStatusUpdate()
                .accept(TaskStatusUpdateEvent.newBuilder().newStatus(Status.FAILED).build());

        // then
        CompletedBuild completedBuild = result.poll(3, TimeUnit.SECONDS);
        Assert.assertNotNull("Missing build result.", completedBuild);
        Assert.assertEquals(
                "The build should fail.",
                BuildStatus.FAILED,
                completedBuild.getBuildResult().getBuildStatus());

        List<Object> executedCommands = buildAgentClientFactory.getBuildAgentClient().getExecutedCommands();
        logger.info("Executed commands {}.", executedCommands);
        Assert.assertEquals(2, executedCommands.size());
        Assertions.assertThat(executedCommands).anySatisfy(c -> ((String) c).contains("startSshd.sh"));
    }

}
