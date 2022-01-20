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

import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;

public class TermdRunningBuild implements RunningBuild {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final RunningEnvironment runningEnvironment;
    private final BuildExecutionConfiguration buildExecutionConfiguration;

    private Consumer<CompletedBuild> onComplete;
    private Consumer<Throwable> onError;

    private Runnable cancelHook;
    private boolean cancelRequested = false;

    public TermdRunningBuild(
            RunningEnvironment runningEnvironment,
            BuildExecutionConfiguration buildExecutionConfiguration,
            Consumer<CompletedBuild> onComplete,
            Consumer<Throwable> onError) {
        this.runningEnvironment = runningEnvironment;
        this.buildExecutionConfiguration = buildExecutionConfiguration;
        this.onComplete = onComplete;
        this.onError = onError;
    }

    public void setCompletedBuild(CompletedBuild completedBuild) {
        logger.debug("Setting completed build {}", completedBuild);
        onComplete.accept(completedBuild);
    }

    public void setBuildError(Throwable error) {
        onError.accept(error);
    }

    @Override
    public RunningEnvironment getRunningEnvironment() {
        return runningEnvironment;
    }

    @Override
    public synchronized void cancel() {
        cancelRequested = true;
        if (cancelHook != null) {
            cancelHook.run();
        } else {
            logger.warn(
                    "Trying to cancel operation while no cancel hook is defined. The operation might be already completed.");
        }
    }

    public String getBuildScript() {
        return buildExecutionConfiguration.getBuildScript();
    }

    public String getName() {
        return buildExecutionConfiguration.getName();
    }

    public String getScmRepoURL() {
        return buildExecutionConfiguration.getScmRepoURL();
    }

    public String getScmRevision() {
        return buildExecutionConfiguration.getScmRevision();
    }

    public synchronized void setCancelHook(Runnable cancelHook) {
        this.cancelHook = cancelHook;
    }

    public boolean isCanceled() {
        return cancelRequested;
    }
}
