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
package org.jboss.pnc.termdbuilddriver;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.spi.builddriver.BuildDriverStatus.CANCELLED;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class TermdBuildDriverTest extends AbstractLocalBuildAgentTest {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Configuration configuration = mock(Configuration.class);

    @Before
    public void before() throws ConfigurationParseException {
        doReturn(new SystemConfig(null, null, null, null, null, null)).when(configuration).getModuleConfig(any());
    }

    @Test(timeout = 60_000)
    public void shouldFetchFromGitAndBuild() throws Exception {
        //given
        Path tmpRepo = Files.createTempDirectory("tmpRepo");
        String repoPath = "file://" + tmpRepo.toAbsolutePath().toString() + "/test-repo";
        ZipUtils.unzipToDir(tmpRepo, "/repo.zip");
        String dirName = "test-repo-cloned";

        TermdBuildDriver driver = new TermdBuildDriver(getConfiguration());
        BuildExecutionSession buildExecution = mock(BuildExecutionSession.class);
        BuildExecutionConfiguration buildExecutionConfiguration = mock(BuildExecutionConfiguration.class);
        doReturn(repoPath).when(buildExecutionConfiguration).getScmRepoURL();
        doReturn("master").when(buildExecutionConfiguration).getScmRevision();
        doReturn("mvn validate").when(buildExecutionConfiguration).getBuildScript();
        doReturn(dirName).when(buildExecutionConfiguration).getName();
        doReturn(buildExecutionConfiguration).when(buildExecution).getBuildExecutionConfiguration();

        AtomicReference<CompletedBuild> buildResult = new AtomicReference<>();

        //when
        RunningBuild runningBuild = driver.startProjectBuild(buildExecution, localEnvironmentPointer);
        runningBuild.monitor(buildResult::set, exception -> fail(exception.getMessage()));

        logger.info("==== shouldFetchFromGitAndBuild logs ====");
        logger.info(buildResult.get().getBuildResult().getBuildLog());
        logger.info("==== /shouldFetchFromGitAndBuild logs ====");

        //then
        assertThat(buildResult.get().getBuildResult()).isNotNull();
        assertThat(buildResult.get().getBuildResult().getBuildLog()).isNotEmpty();
        assertThat(Files.exists(localEnvironmentPointer.getWorkingDirectory())).isTrue();
        assertThat(Files.exists(localEnvironmentPointer.getWorkingDirectory().resolve(dirName))).isTrue();
    }

    @Test
    public void shouldStartAndCancelTheCommand() throws ConfigurationParseException, BuildDriverException {
        //given
        String dirName = "test-workdir";
        String logStart = "Running the command...";
        String logEnd = "Command completed.";

        TermdBuildDriver driver = new TermdBuildDriver(getConfiguration());
        BuildExecutionSession buildExecution = mock(BuildExecutionSession.class);
        BuildExecutionConfiguration buildExecutionConfiguration = mock(BuildExecutionConfiguration.class);
        doReturn("echo \"" + logStart + "\"; mvn sleep").when(buildExecutionConfiguration).getBuildScript(); //TODO fix command
        doReturn(dirName).when(buildExecutionConfiguration).getName();
        doReturn(buildExecutionConfiguration).when(buildExecution).getBuildExecutionConfiguration();

        AtomicReference<CompletedBuild> buildResult = new AtomicReference<>();

        //when
        RunningBuild runningBuild = driver.startProjectBuild(buildExecution, localEnvironmentPointer);
        runningBuild.monitor(buildResult::set, exception -> fail(exception.getMessage()));
        runningBuild.cancel();

        logger.info(buildResult.get().getBuildResult().getBuildLog());

        //then
        assertThat(buildResult.get().getBuildResult()).isNotNull();
        assertThat(buildResult.get().getBuildResult().getBuildLog()).contains(logStart);
        assertThat(buildResult.get().getBuildResult().getBuildLog()).doesNotContain(logEnd);
        assertThat(buildResult.get().getBuildResult().getBuildDriverStatus()).isEqualTo(CANCELLED);
    }

    private Configuration getConfiguration() throws ConfigurationParseException {
        SystemConfig systemConfig = mock(SystemConfig.class);
        Configuration configuration = mock(Configuration.class);
        doReturn(systemConfig).when(configuration).getModuleConfig(any());
        return configuration;
    }

}
