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

import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class TermdRunningBuild implements RunningBuild {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final long MAX_TIMEOUT = 2;
    private static final TimeUnit MAX_TIMEOUT_UNIT = TimeUnit.HOURS;

    private final RunningEnvironment runningEnvironment;
    private final BuildExecutionConfiguration buildExecutionConfiguration;

    private CompletableFuture<CompletedBuild> buildPromise = new CompletableFuture<>();

    public TermdRunningBuild(RunningEnvironment runningEnvironment, BuildExecutionConfiguration buildExecutionConfiguration) {
        this.runningEnvironment = runningEnvironment;
        this.buildExecutionConfiguration = buildExecutionConfiguration;
    }

    @Override
    public void monitor(Consumer<CompletedBuild> onComplete, Consumer<Throwable> onError) {
        try {
            logger.debug("[{}] The client started monitoring the build", runningEnvironment.getId());
            onComplete.accept(buildPromise.get(MAX_TIMEOUT, MAX_TIMEOUT_UNIT));
        } catch (InterruptedException | TimeoutException e) {
            onError.accept(e);
        } catch (ExecutionException e) {
            onError.accept(e.getCause());
        }
    }

    public void setCompletedBuild(CompletedBuild completedBuild) {
        logger.debug("[{}] Setting completed build {}", runningEnvironment.getId(), completedBuild);
        buildPromise.complete(completedBuild);
    }

    public void setBuildPromiseError(Exception error) {
        buildPromise.completeExceptionally(error);
    }

    @Override
    public RunningEnvironment getRunningEnvironment() {
        return runningEnvironment;
    }

    @Override
    public void cancel() {

    }

    public String getBuildScript() {
        return buildExecutionConfiguration.getBuildScript();
    }

    public String getName() {
        return buildExecutionConfiguration.getName();
    }

    public String getScmRepoURL() { //TODO determine this before calling the execution
        if (buildExecutionConfiguration.getScmMirrorRepoURL() != null) {
            return buildExecutionConfiguration.getScmMirrorRepoURL();
        } else {
            return buildExecutionConfiguration.getScmRepoURL();
        }
    }

    public String getScmRevision() { //TODO determine this before calling the execution
        if (buildExecutionConfiguration.getScmMirrorRevision() != null) {
            return buildExecutionConfiguration.getScmMirrorRevision();
        } else {
            return buildExecutionConfiguration.getScmRevision();
        }
    }

}
