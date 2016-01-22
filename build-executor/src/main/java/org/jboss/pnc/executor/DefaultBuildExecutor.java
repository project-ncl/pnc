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

package org.jboss.pnc.executor;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.executor.exceptions.BuildProcessException;
import org.jboss.pnc.executor.servicefactories.BuildDriverFactory;
import org.jboss.pnc.executor.servicefactories.EnvironmentDriverFactory;
import org.jboss.pnc.executor.servicefactories.RepositoryManagerFactory;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.BuildExecutionStatus;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.BuildDriverStatus;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.environment.DestroyableEnvironment;
import org.jboss.pnc.spi.environment.EnvironmentDriver;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.environment.StartedEnvironment;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.events.BuildExecutionStatusChangedEvent;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.jboss.pnc.spi.executor.BuildExecutor;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.jboss.pnc.spi.repositorymanager.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class DefaultBuildExecutor implements BuildExecutor {

    private final Logger log = LoggerFactory.getLogger(DefaultBuildExecutor.class);

    private ExecutorService executor;

    private RepositoryManagerFactory repositoryManagerFactory;
    private BuildDriverFactory buildDriverFactory;
    private EnvironmentDriverFactory environmentDriverFactory;
    private final Map<Integer, BuildExecutionSession> runningExecutions = new HashMap<>();

    @Deprecated
    public DefaultBuildExecutor() {}; //CDI workaround for constructor injection

    @Inject
    public DefaultBuildExecutor(
            RepositoryManagerFactory repositoryManagerFactory,
            BuildDriverFactory buildDriverFactory,
            EnvironmentDriverFactory environmentDriverFactory,
            Configuration configuration) {

        this.repositoryManagerFactory = repositoryManagerFactory;
        this.buildDriverFactory = buildDriverFactory;
        this.environmentDriverFactory = environmentDriverFactory;

        int executorThreadPoolSize = 12;
        try {
            String executorThreadPoolSizeStr = configuration.getModuleConfig(new PncConfigProvider<>(SystemConfig.class)).getExecutorThreadPoolSize();
            if (executorThreadPoolSizeStr != null) {
                executorThreadPoolSize = Integer.parseInt(executorThreadPoolSizeStr);
            }
        } catch (ConfigurationParseException e) {
            log.warn("Unable parse config. Using defaults.");
        }

        executor = Executors.newFixedThreadPool(executorThreadPoolSize);
    }


    @Override
    public BuildExecutionSession startBuilding(
            BuildExecutionConfiguration buildExecutionConfiguration,
            Consumer<BuildExecutionStatusChangedEvent> onBuildExecutionStatusChangedEvent) throws ExecutorException {

        BuildExecutionSession buildExecutionSession = new DefaultBuildExecutionSession(buildExecutionConfiguration, onBuildExecutionStatusChangedEvent);
        buildExecutionSession.setStatus(BuildExecutionStatus.NEW);

        //TODO update logging: log.info("Staring build execution task: {}; Build Configuration id: {}.", buildExecutionConfiguration.getId(), buildExecutionConfiguration.etBuildConfiguration().getId());

        runningExecutions.put(buildExecutionConfiguration.getId(), buildExecutionSession);

        CompletableFuture.supplyAsync(() -> configureRepository(buildExecutionSession), executor)
                .thenApplyAsync(repositoryConfiguration -> setUpEnvironment(buildExecutionSession, repositoryConfiguration), executor)
                .thenComposeAsync(startedEnvironment -> waitForEnvironmentInitialization(buildExecutionSession, startedEnvironment), executor)
                .thenApplyAsync(nul -> buildSetUp(buildExecutionSession), executor)
                .thenComposeAsync(runningBuild -> waitBuildToComplete(buildExecutionSession, runningBuild), executor)
                .thenApplyAsync(completedBuild -> retrieveBuildDriverResults(buildExecutionSession, completedBuild), executor)
                .thenApplyAsync(nul -> retrieveRepositoryManagerResults(buildExecutionSession), executor)
                .thenApplyAsync(nul -> destroyEnvironment(buildExecutionSession), executor)
                .handleAsync((nul, e) -> completeExecution(buildExecutionSession, e), executor);

        //TODO re-connect running instances in case of crash
        return buildExecutionSession;
    }

    @Override
    public BuildExecutionSession getRunningExecution(int buildExecutionTaskId) {
        return runningExecutions.get(buildExecutionTaskId);
    }

    private RepositorySession configureRepository(BuildExecutionSession buildExecutionSession) {
        buildExecutionSession.setStatus(BuildExecutionStatus.REPO_SETTING_UP);
        try {
            RepositoryManager repositoryManager = repositoryManagerFactory.getRepositoryManager(RepositoryType.MAVEN);
            BuildExecution buildExecution = buildExecutionSession.getBuildExecutionConfiguration();
            return repositoryManager.createBuildRepository(buildExecution);
        } catch (Throwable e) {
            throw new BuildProcessException(e);
        }
    }

    private StartedEnvironment setUpEnvironment(BuildExecutionSession buildExecutionSession, RepositorySession repositorySession) {
        buildExecutionSession.setStatus(BuildExecutionStatus.BUILD_ENV_SETTING_UP);
        BuildExecutionConfiguration buildExecutionConfiguration = buildExecutionSession.getBuildExecutionConfiguration();
        try {
            EnvironmentDriver envDriver = environmentDriverFactory.getDriver(buildExecutionConfiguration.getBuildType());
            StartedEnvironment startedEnv = envDriver.buildEnvironment(
                    buildExecutionConfiguration.getBuildType(),
                    repositorySession);
            return startedEnv;
        } catch (Throwable e) {
            throw new BuildProcessException(e);
        }
    }

    private CompletableFuture<Void> waitForEnvironmentInitialization(BuildExecutionSession buildExecutionSession, StartedEnvironment startedEnvironment) {
        CompletableFuture<Void> waitToCompleteFuture = new CompletableFuture<>();
        try {
            Consumer<RunningEnvironment> onComplete = (runningEnvironment) -> {
                buildExecutionSession.setRunningEnvironment(runningEnvironment);
                buildExecutionSession.setStatus(BuildExecutionStatus.BUILD_ENV_SETUP_COMPLETE_SUCCESS);
                waitToCompleteFuture.complete(null);
            };
            Consumer<Exception> onError = (e) -> {
                buildExecutionSession.setStatus(BuildExecutionStatus.BUILD_ENV_SETUP_COMPLETE_WITH_ERROR);
                waitToCompleteFuture.completeExceptionally(new BuildProcessException(e, startedEnvironment));
            };
            buildExecutionSession.setStatus(BuildExecutionStatus.BUILD_ENV_WAITING);

            startedEnvironment.monitorInitialization(onComplete, onError);
        } catch (Throwable e) {
            waitToCompleteFuture.completeExceptionally(new BuildProcessException(e, startedEnvironment));
        }
        return waitToCompleteFuture;
    }

    private RunningBuild buildSetUp(BuildExecutionSession buildExecutionSession) {
        buildExecutionSession.setStatus(BuildExecutionStatus.BUILD_SETTING_UP);
        RunningEnvironment runningEnvironment = buildExecutionSession.getRunningEnvironment();
        try {
            String liveLogWebSocketUrl = runningEnvironment.getBuildAgentUrl();
            log.debug("Setting live log websocket url: {}", liveLogWebSocketUrl);
            buildExecutionSession.setLiveLogsUri(Optional.of(new URI(liveLogWebSocketUrl)));
            buildExecutionSession.setStartTime(new Date());
            BuildDriver buildDriver = buildDriverFactory.getBuildDriver(buildExecutionSession.getBuildExecutionConfiguration().getBuildType());
            return buildDriver.startProjectBuild(buildExecutionSession, runningEnvironment);
        } catch (Throwable e) {
            throw new BuildProcessException(e, runningEnvironment);
        }
    }

    private CompletableFuture<CompletedBuild> waitBuildToComplete(BuildExecutionSession buildExecutionSession, RunningBuild runningBuild) {
        CompletableFuture<CompletedBuild> waitToCompleteFuture = new CompletableFuture<>();
        try {
            Consumer<CompletedBuild> onComplete = (completedBuild) -> {
                waitToCompleteFuture.complete(completedBuild);
            };
            Consumer<Throwable> onError = (e) -> {
                waitToCompleteFuture.completeExceptionally(new BuildProcessException(e, runningBuild.getRunningEnvironment()));
            };

            buildExecutionSession.setStatus(BuildExecutionStatus.BUILD_WAITING);

            runningBuild.monitor(onComplete, onError);
        } catch (Throwable exception) {
            waitToCompleteFuture.completeExceptionally(
                    new BuildProcessException(exception, runningBuild.getRunningEnvironment()));
        }
        return waitToCompleteFuture;
    }

    private Void retrieveBuildDriverResults(BuildExecutionSession buildExecutionSession, CompletedBuild completedBuild) {
        buildExecutionSession.setStatus(BuildExecutionStatus.COLLECTING_RESULTS_FROM_BUILD_DRIVER);
        try {
            BuildDriverResult buildResult = completedBuild.getBuildResult();
            BuildDriverStatus buildDriverStatus = buildResult.getBuildDriverStatus();
            buildExecutionSession.setBuildDriverResult(buildResult);
            if (buildDriverStatus.completedSuccessfully()) {
                buildExecutionSession.setStatus(BuildExecutionStatus.BUILD_COMPLETED_SUCCESS);
            } else {
                buildExecutionSession.setStatus(BuildExecutionStatus.BUILD_COMPLETED_WITH_ERROR);
            }
            return null;
        } catch (Throwable e) {
            throw new BuildProcessException(e, completedBuild.getRunningEnvironment());
        }
    }

    private Void retrieveRepositoryManagerResults(BuildExecutionSession buildExecutionSession) {
        try {
            buildExecutionSession.setStatus(BuildExecutionStatus.COLLECTING_RESULTS_FROM_REPOSITORY_NAMAGER);
            RunningEnvironment runningEnvironment = buildExecutionSession.getRunningEnvironment();
            buildExecutionSession.setRunningEnvironment(runningEnvironment);

            RepositorySession repositorySession = runningEnvironment.getRepositorySession();
            RepositoryManagerResult repositoryManagerResult = repositorySession.extractBuildArtifacts();
            buildExecutionSession.setRepositoryManagerResult(repositoryManagerResult);
        } catch (Throwable e) {
            throw new BuildProcessException(e, buildExecutionSession.getRunningEnvironment());
        }
        return null;
    }

    private Void destroyEnvironment(BuildExecutionSession buildExecutionSession) {
        try {
            buildExecutionSession.setStatus(BuildExecutionStatus.BUILD_ENV_DESTROYING);
            buildExecutionSession.getRunningEnvironment().destroyEnvironment();
            buildExecutionSession.setStatus(BuildExecutionStatus.BUILD_ENV_DESTROYED);
        } catch (Throwable e) {
            throw new BuildProcessException(e);
        }
        return null;
    }

    private Void completeExecution(BuildExecutionSession buildExecutionSession, Throwable e) {
        buildExecutionSession.setStatus(BuildExecutionStatus.FINALIZING_EXECUTION);

        if (buildExecutionSession.getStartTime() == null) {
            buildExecutionSession.setException(new ExecutorException("Missing start time."));
        }
        if (e != null) {
            stopRunningEnvironment(e);
        }

        if (e != null) {
            buildExecutionSession.setException(new ExecutorException(e));
        }

        if (buildExecutionSession.getEndTime() != null) {
            buildExecutionSession.setException(new ExecutorException("End time already set."));
        } else {
            buildExecutionSession.setEndTime(new Date());
        }

        //check if any of previous statuses indicated "failed" state
        if (buildExecutionSession.hasFailed()) { //TODO differentiate build and system error
            buildExecutionSession.setStatus(BuildExecutionStatus.DONE_WITH_ERRORS);
        } else {
            buildExecutionSession.setStatus(BuildExecutionStatus.DONE);
        }

        log.debug("Removing buildExecutionTask [" + buildExecutionSession.getId() + "] form list of running tasks.");
        runningExecutions.remove(buildExecutionSession.getId());

        return null;
    }

    /**
     * Tries to stop running environment if the exception contains information about running environment
     *
     * @param ex Exception in build process (To stop the environment it has to be instance of BuildProcessException)
     */
    private void stopRunningEnvironment(Throwable ex) {
        DestroyableEnvironment destroyableEnvironment = null;
        if(ex instanceof BuildProcessException) {
            BuildProcessException bpEx = (BuildProcessException) ex;
            destroyableEnvironment = bpEx.getDestroyableEnvironment();
        } else if(ex.getCause() instanceof BuildProcessException) {
            BuildProcessException bpEx = (BuildProcessException) ex.getCause();
            destroyableEnvironment = bpEx.getDestroyableEnvironment();
        } else {
            //It shouldn't never happen - Throwable should be caught in all steps of build chain
            //and BuildProcessException should be thrown instead of that
            log.warn("Possible leak of a running environment! Build process ended with exception, "
                    + "but the exception didn't contain information about running environment.", ex);
        }

        try {
            if (destroyableEnvironment != null)
                destroyableEnvironment.destroyEnvironment();

        } catch (EnvironmentDriverException envE) {
            log.warn("Running environment" + destroyableEnvironment + " couldn't be destroyed!", envE);
        }
    }

    @Override
    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}
