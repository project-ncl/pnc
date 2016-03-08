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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import org.assertj.core.api.Assertions;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.spi.builddriver.BuildDriverStatus;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.jboss.pnc.termdbuilddriver.commands.InvocatedCommandResult;
import org.jboss.pnc.termdbuilddriver.commands.TermdCommandBatchExecutionResult;
import org.jboss.pnc.termdbuilddriver.commands.TermdCommandExecutionException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class TermdBuildDriverTest extends AbstractLocalBuildAgentTest {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    BuildConfigurationAudited jsr107BuildConfig;
    BuildExecutionSession buildExecutionMock;
    
    private Configuration configuration = mock(Configuration.class);

    @Before
    public void before() throws ConfigurationParseException {
        jsr107BuildConfig = mock(BuildConfigurationAudited.class);
        doReturn("https://github.com/jsr107/jsr107spec.git").when(jsr107BuildConfig).getScmRepoURL();
        doReturn("master").when(jsr107BuildConfig).getScmRevision();
        doReturn("mvn validate").when(jsr107BuildConfig).getBuildScript();
        doReturn("jsr107-test").when(jsr107BuildConfig).getName();
        
        doReturn(new SystemConfig(null, null, null, "")).when(configuration).getModuleConfig(any());
        
        
        buildExecutionMock = mock(BuildExecutionSession.class);
        BuildExecutionConfiguration buildExecutionConfiguration = mock(BuildExecutionConfiguration.class);
        doReturn(buildExecutionConfiguration).when(buildExecutionMock).getBuildExecutionConfiguration();
    }

    @Test(timeout = 60_000)
    public void shouldFailOnRemoteScriptInvokationException() throws Exception {
        //given
        TermdBuildDriver driver = new TermdBuildDriver(configuration) {
            @Override
            protected CompletableFuture<TermdCommandBatchExecutionResult> invokeRemoteScript(
                    TermdRunningBuild termdRunningBuild,
                    String scriptPath,
                    BuildExecutionSession currentBuildExecution) {
                return CompletableFuture.supplyAsync(() -> {
                    throw new TermdCommandExecutionException("let's check it!", new NullPointerException());
                });
            }
        };

        //when
        RunningBuild runningBuild = driver.startProjectBuild(buildExecutionMock, localEnvironmentPointer);

        //then
        runningBuild.monitor(completedBuild -> fail("this execution should fail"), exception -> System.out.println("OK"));
    }

    @Test(timeout = 60_000)
    public void shouldReportBuildWithFailureWhenRemoteCommandFails() throws Exception {
        //given
        TermdBuildDriver driver = new TermdBuildDriver(configuration) {
            @Override
            protected CompletableFuture<String> uploadScript(TermdRunningBuild termdRunningBuild, StringBuilder commandAppender) {
                return CompletableFuture.completedFuture("run.sh");
            }

            @Override
            protected CompletableFuture<TermdCommandBatchExecutionResult> invokeRemoteScript(
                    TermdRunningBuild termdRunningBuild,
                    String scriptPath,
                    BuildExecutionSession currentBuildExecution) {
                return CompletableFuture.supplyAsync(() -> {
                    InvocatedCommandResult result = mock(InvocatedCommandResult.class);
                    doReturn(false).when(result).isSucceed();
                    return new TermdCommandBatchExecutionResult(Arrays.asList(result));
                });
            }

            @Override
            protected CompletableFuture<StringBuffer> aggregateLogs(TermdRunningBuild termdRunningBuild, TermdCommandBatchExecutionResult allInvokedCommands) {
                return CompletableFuture.completedFuture(new StringBuffer("Ignoring logs"));
            }
        };

        AtomicReference<CompletedBuild> buildResult = new AtomicReference<>();

        //when
        RunningBuild runningBuild = driver.startProjectBuild(buildExecutionMock, localEnvironmentPointer);
        runningBuild.monitor(completedBuild -> buildResult.set(completedBuild), exception -> Assertions.fail("Unexpected error", exception));

        //then
        assertThat(buildResult.get().getBuildResult().getBuildDriverStatus()).isEqualTo(BuildDriverStatus.FAILED);
    }

    @Test(timeout = 60_000) @Ignore //ignoring as WebSocket Log URL is provided by EnvDriver
    public void shouldSetLogsInBuildExecution() throws Exception {
        //given
        TermdBuildDriver driver = new TermdBuildDriver() {
            @Override
            protected CompletableFuture<String> uploadScript(TermdRunningBuild termdRunningBuild, StringBuilder commandAppender) {
                return CompletableFuture.completedFuture("run.sh");
            }

            @Override
            protected CompletableFuture<StringBuffer> aggregateLogs(TermdRunningBuild termdRunningBuild, TermdCommandBatchExecutionResult allInvokedCommands) {
                return CompletableFuture.completedFuture(new StringBuffer("Ignoring logs"));
            }
        };

        //when
        RunningBuild runningBuild = driver.startProjectBuild(buildExecutionMock, localEnvironmentPointer);
        runningBuild.monitor(completedBuild -> {}, exception -> Assertions.fail("Unexpected error", exception));

        //then
//TODO move to envDriver
//        verify(buildExecutionMock, times(1)).setLogsWebSocketLink(any(URI.class));
//        verify(buildExecutionMock, times(1)).clearLogsWebSocketLink();
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

    private Configuration getConfiguration() throws ConfigurationParseException {
        SystemConfig systemConfig = mock(SystemConfig.class);
        Configuration configuration = mock(Configuration.class);
        doReturn(systemConfig).when(configuration).getModuleConfig(any());
        return configuration;
    }

}