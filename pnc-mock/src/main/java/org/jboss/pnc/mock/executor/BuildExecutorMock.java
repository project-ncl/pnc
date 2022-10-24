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

package org.jboss.pnc.mock.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.pnc.common.concurrent.MDCExecutors;
import org.jboss.pnc.common.concurrent.NamedThreadFactory;
import org.jboss.pnc.enums.BuildExecutionStatus;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.pnc.mock.spi.BuildDriverResultMock;
import org.jboss.pnc.mock.spi.RepositoryManagerResultMock;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.events.BuildExecutionStatusChangedEvent;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.jboss.pnc.spi.executor.BuildExecutor;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class BuildExecutorMock implements BuildExecutor {

    private final Logger log = LoggerFactory.getLogger(BuildExecutorMock.class);

    private final Map<String, BuildExecutionSession> runningExecutions = new HashMap<>();

    private final ExecutorService executor = MDCExecutors
            .newFixedThreadPool(4, new NamedThreadFactory("build-executor-mock"));

    private final Map<String, CompletableFuture<Integer>> runningFutures = new HashMap<>();

    @Override
    public BuildExecutionSession startBuilding(
            BuildExecutionConfiguration buildExecutionConfiguration,
            Consumer<BuildExecutionStatusChangedEvent> onBuildExecutionStatusChangedEvent,
            String accessToken) throws ExecutorException {

        log.info(
                "Starting mock build execution for buildExecutionConfiguration.id {}",
                buildExecutionConfiguration.getId());

        BuildExecutionSession buildExecutionSession = new BuildExecutionSessionMock(
                buildExecutionConfiguration,
                onBuildExecutionStatusChangedEvent);
        buildExecutionSession.setStatus(BuildExecutionStatus.NEW);

        runningExecutions.put(buildExecutionConfiguration.getId(), buildExecutionSession);
        Consumer<BuildExecutionStatus> onCompleteInternal = (buildStatus) -> {
            log.debug(
                    "Removing buildExecutionConfiguration.id [" + buildExecutionConfiguration.getId()
                            + "] form list of running tasks.");
            runningExecutions.remove(buildExecutionConfiguration.getId());
            buildExecutionSession.setStatus(buildStatus);
        };

        CompletableFuture<Integer> future = CompletableFuture
                .supplyAsync(() -> mockBuild(buildExecutionSession), executor)
                .handleAsync((buildPassed, e) -> complete(buildPassed, e, onCompleteInternal), executor);
        runningFutures.put(buildExecutionConfiguration.getId(), future);
        return buildExecutionSession;
    }

    private Integer complete(Boolean buildPassed, Throwable e, Consumer<BuildExecutionStatus> onCompleteInternal) {
        if (e != null) {
            log.error("Error in mock build.", e);
        }

        log.debug("Completing mock build.");
        if (buildPassed) {
            onCompleteInternal.accept(BuildExecutionStatus.DONE);
        } else {
            onCompleteInternal.accept(BuildExecutionStatus.DONE_WITH_ERRORS);
        }
        return -1;
    }

    private Boolean mockBuild(BuildExecutionSession buildExecutionSession) {
        log.debug("Building {}.", buildExecutionSession.getId());
        BuildDriverResult driverResult;
        Boolean buildPassed;
        if (TestProjectConfigurationBuilder.FAIL
                .equals(buildExecutionSession.getBuildExecutionConfiguration().getBuildScript())) {
            log.debug("Marking build {} as Failed.", buildExecutionSession.getId());
            driverResult = BuildDriverResultMock.mockResult(BuildStatus.FAILED);
            buildExecutionSession.setStatus(BuildExecutionStatus.BUILD_COMPLETED_WITH_ERROR);
            buildPassed = false;
        } else if (TestProjectConfigurationBuilder.FAIL_WITH_DELAY
                .equals(buildExecutionSession.getBuildExecutionConfiguration().getBuildScript())) {
            log.debug("Waiting for a while for a build {}.", buildExecutionSession.getId());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.warn("Build mock has been interrupted.", e);
            }
            log.debug("Marking build {} as Failed.", buildExecutionSession.getId());
            driverResult = BuildDriverResultMock.mockResult(BuildStatus.FAILED);
            buildPassed = false;
        } else if (TestProjectConfigurationBuilder.CANCEL
                .equals(buildExecutionSession.getBuildExecutionConfiguration().getBuildScript())) {
            log.debug("Waiting for a while for a build {} to be canceled.", buildExecutionSession.getId());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.warn("Build mock has been interrupted.", e);
            }
            driverResult = BuildDriverResultMock.mockResult(BuildStatus.CANCELLED);
            buildPassed = false;
        } else {
            log.debug("Marking build {} as Success.", buildExecutionSession.getId());
            driverResult = BuildDriverResultMock.mockResult(BuildStatus.SUCCESS);
            RepositoryManagerResult repositoryManagerResult = RepositoryManagerResultMock.mockResult();
            buildExecutionSession.setRepositoryManagerResult(repositoryManagerResult);
            buildPassed = true;
        }

        buildExecutionSession.setBuildDriverResult(driverResult);
        return buildPassed;
    }

    @Override
    public BuildExecutionSession getRunningExecution(String buildExecutionTaskId) {
        return runningExecutions.get(buildExecutionTaskId);
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void cancel(String executionConfigurationId) throws ExecutorException {
        BuildExecutionSession buildExecutionSession = runningExecutions.get(executionConfigurationId);
        if (buildExecutionSession == null) {
            log.error("Unable to cancel build {}. The build is not running.", executionConfigurationId);
            return;
        }
        log.info("Cancelling build {}.", executionConfigurationId);
        runningFutures.get(executionConfigurationId).cancel(true);
        BuildDriverResult driverResult = BuildDriverResultMock.mockResult(BuildStatus.CANCELLED);
        buildExecutionSession.setBuildDriverResult(driverResult);
    }

    public void addRunningExecution(String id, BuildExecutionSession session) {
        runningExecutions.put(id, session);
    }
}
