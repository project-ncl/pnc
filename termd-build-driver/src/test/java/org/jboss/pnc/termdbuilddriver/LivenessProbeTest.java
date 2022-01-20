/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleconfig.TermdBuildDriverModuleConfig;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class LivenessProbeTest {

    Logger logger = LoggerFactory.getLogger(LivenessProbeTest.class);

    private String HOST = "localhost";

    SystemConfig systemConfig = mock(SystemConfig.class);

    @Test
    public void shouldFailTheBuildWhenAgentIsNotResponding() throws InterruptedException, BuildDriverException {

        TermdBuildDriverModuleConfig buildDriverModuleConfig = mock(TermdBuildDriverModuleConfig.class);
        doReturn(200L).when(buildDriverModuleConfig).getLivenessProbeFrequencyMillis();
        doReturn(500L).when(buildDriverModuleConfig).getLivenessFailTimeoutMillis();

        ClientMockFactory buildAgentClientMockFactory = new ClientMockFactory();
        TermdBuildDriver driver = new TermdBuildDriver(
                systemConfig,
                buildDriverModuleConfig,
                buildAgentClientMockFactory);

        BuildExecutionSession buildExecution = mock(BuildExecutionSession.class);
        BuildExecutionConfiguration buildExecutionConfiguration = mock(BuildExecutionConfiguration.class);
        doReturn(buildExecutionConfiguration).when(buildExecution).getBuildExecutionConfiguration();

        RunningEnvironment runningEnvironment = mock(RunningEnvironment.class);
        doReturn(Paths.get("")).when(runningEnvironment).getWorkingDirectory();
        doReturn(new DebugData(false)).when(runningEnvironment).getDebugData();
        doReturn("http://localhost/").when(runningEnvironment).getInternalBuildAgentUrl();
        doReturn(runningEnvironment).when(buildExecution).getRunningEnvironment();

        BlockingQueue<Throwable> result = new ArrayBlockingQueue(1);

        Consumer<CompletedBuild> onComplete = (completedBuild) -> Assert.fail("Build should complete with error.");
        Consumer<Throwable> onError = (throwable) -> {
            try {
                result.put(throwable);
            } catch (InterruptedException e) {
                Assert.fail("Error in the test. Unable to add the result to queue.");
            }
        };

        // when
        RunningBuild runningBuild = driver.startProjectBuild(buildExecution, runningEnvironment, onComplete, onError);

        // then
        Throwable throwable = result.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull("It should complete with an exception.", throwable);
        Assert.assertEquals("Build Agent has gone away.", throwable.getMessage());
    }

}
