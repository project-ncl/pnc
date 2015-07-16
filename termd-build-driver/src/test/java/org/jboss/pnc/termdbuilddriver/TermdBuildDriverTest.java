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

import org.assertj.core.api.Assertions;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.spi.builddriver.BuildDriverStatus;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.termdbuilddriver.commands.InvocatedCommandResult;
import org.jboss.pnc.termdbuilddriver.commands.TermdCommandBatchExecutionResult;
import org.jboss.pnc.termdbuilddriver.commands.TermdCommandExecutionException;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class TermdBuildDriverTest extends AbstractLocalBuildAgentTest {

    BuildConfiguration jsr107BuildConfig;

    @Before
    public void before() {
        jsr107BuildConfig = mock(BuildConfiguration.class);
        doReturn("https://github.com/jsr107/jsr107spec.git").when(jsr107BuildConfig).getScmRepoURL();
        doReturn("master").when(jsr107BuildConfig).getScmRevision();
        doReturn("mvn validate").when(jsr107BuildConfig).getBuildScript();
        doReturn("jsr107-test").when(jsr107BuildConfig).getName();
    }

    @Test(timeout = 60_000)
    public void shouldFailOnRemoteScriptInvokationException() throws Exception {
        //given
        TermdBuildDriver driver = new TermdBuildDriver() {
            @Override
            protected CompletableFuture<TermdCommandBatchExecutionResult> invokeRemoteScript(TermdRunningBuild termdRunningBuild,
                    String scriptPath) {
                return CompletableFuture.supplyAsync(() -> {
                    throw new TermdCommandExecutionException("let's check it!", new NullPointerException());
                });
            }
        };

        //when
        RunningBuild runningBuild = driver.startProjectBuild(jsr107BuildConfig, localEnvironmentPointer);

        //then
        runningBuild.monitor(completedBuild -> fail("this execution should fail"), exception -> System.out.println("OK"));
    }

    @Test(timeout = 60_000)
    public void shouldReportBuildWithFailureWhenRemoteCommandFails() throws Exception {
        //given
        TermdBuildDriver driver = new TermdBuildDriver() {
            @Override
            protected CompletableFuture<String> uploadScript(TermdRunningBuild termdRunningBuild, StringBuilder commandAppender) {
                return CompletableFuture.completedFuture("run.sh");
            }

            @Override
            protected CompletableFuture<TermdCommandBatchExecutionResult> invokeRemoteScript(TermdRunningBuild termdRunningBuild,
                    String scriptPath) {
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
        RunningBuild runningBuild = driver.startProjectBuild(jsr107BuildConfig, localEnvironmentPointer);
        runningBuild.monitor(completedBuild -> buildResult.set(completedBuild), exception -> Assertions.fail("Unexpected error", exception));

        //then
        assertThat(buildResult.get().getBuildResult().getBuildDriverStatus()).isEqualTo(BuildDriverStatus.FAILED);
    }
}