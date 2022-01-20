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
package org.jboss.pnc.mock.builddriver;

import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-24.
 */
public abstract class BuildDriverBase {

    public static final Logger log = LoggerFactory.getLogger(BuildDriverBase.class);

    private BuildStatus buildStatus;

    public RunningBuild startProjectBuild(
            BuildExecutionSession buildExecutionSession,
            RunningEnvironment runningEnvironment,
            Consumer<CompletedBuild> onComplete,
            Consumer<Throwable> onError) {

        log.debug("Building " + buildExecutionSession.getId());

        Thread thread = new Thread(() -> {
            try {
                complete(buildExecutionSession, runningEnvironment, onComplete);
            } catch (InterruptedException e) {
                log.error("Interrupted", e);
            }
        });
        thread.start();

        return createRunningBuild(runningEnvironment);
    }

    protected abstract RunningBuild createRunningBuild(RunningEnvironment runningEnvironment);

    abstract void complete(
            BuildExecutionSession buildExecutionSession,
            RunningEnvironment runningEnvironment,
            Consumer<CompletedBuild> onComplete) throws InterruptedException;

    protected void setBuildStatus(String buildScript) {
        if (buildScript.equals(TestProjectConfigurationBuilder.FAIL)) {
            buildStatus = BuildStatus.FAILED;
        } else if (buildScript.equals(TestProjectConfigurationBuilder.CANCEL)) {
            buildStatus = BuildStatus.CANCELLED;
        } else {
            buildStatus = BuildStatus.SUCCESS;
        }
    }

    protected BuildDriverResult getBuildResultMock(final RunningEnvironment runningEnvironment) {
        return new BuildDriverResult() {
            @Override
            public String getBuildLog() {
                return "Building in workspace ... Finished: SUCCESS";
            }

            @Override
            public BuildStatus getBuildStatus() {
                return buildStatus;
            }

            @Override
            public Optional<String> getOutputChecksum() {
                return Optional.of("5678bbe366b11f7216bd03ad33f583d9");
            }

        };
    }

}
