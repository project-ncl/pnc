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

package org.jboss.pnc.core.builder.executor;

import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.EnvironmentDriverFactory;
import org.jboss.pnc.core.RepositoryManagerFactory;
import org.jboss.pnc.core.builder.coordinator.BuildSetTask;
import org.jboss.pnc.core.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.core.builder.DefaultBuildResult;
import org.jboss.pnc.core.exception.BuildProcessException;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.BuildDriverStatus;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.environment.DestroyableEnvironment;
import org.jboss.pnc.spi.environment.EnvironmentDriver;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.environment.StartedEnvironment;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class BuildExecutor {

    private Logger log = LoggerFactory.getLogger(BuildExecutor.class);

//    @Resource
//    private ManagedThreadFactory threadFactory;
    private ExecutorService executor = Executors.newFixedThreadPool(4); //TODO configurable
    //TODO override executor and implement "protected void afterExecute(Runnable r, Throwable t) { }" to catch possible exceptions

    private Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier;
    private DatastoreAdapter datastoreAdapter;
    private RepositoryManagerFactory repositoryManagerFactory;
    private BuildDriverFactory buildDriverFactory;
    private EnvironmentDriverFactory environmentDriverFactory;
    private Map<Integer, BuildExecutionTask> runningExecutions = new HashMap<>();


    @Deprecated
    public BuildExecutor() {}; //CDI workaround for constructor injection

    @Inject
    public BuildExecutor(Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier, DatastoreAdapter datastoreAdapter, RepositoryManagerFactory repositoryManagerFactory, BuildDriverFactory buildDriverFactory, EnvironmentDriverFactory environmentDriverFactory) {
        this.buildStatusChangedEventNotifier = buildStatusChangedEventNotifier;
        this.datastoreAdapter = datastoreAdapter;
        this.repositoryManagerFactory = repositoryManagerFactory;
        this.buildDriverFactory = buildDriverFactory;
        this.environmentDriverFactory = environmentDriverFactory;
    }

    public BuildExecutionTask build(
            BuildConfiguration buildConfiguration,
            BuildConfigurationAudited buildConfigAudited,
            User user,
            Consumer<BuildStatus> onComplete,
            Set<Integer> buildRecordSetIds,
            Integer buildConfigSetRecordId,
            Integer buildTaskId,
            Date submitTime) throws CoreException {

        Integer nextBuildRecordId = datastoreAdapter.getNextBuildRecordId();

        BuildExecutionTask buildExecutionTask = BuildExecutionTask.build(
                nextBuildRecordId,
                buildConfiguration,
                buildConfigAudited,
                user,
                buildRecordSetIds,
                buildConfigSetRecordId,
                Optional.of(buildStatusChangedEventNotifier),
                buildTaskId,
                submitTime
        );

        //TODO recollect to running instances in case of system failure
        startBuilding(buildExecutionTask, onComplete);

        return buildExecutionTask;
    }

    public void startBuilding(BuildExecutionTask buildExecutionTask, Consumer<BuildStatus> onComplete) throws CoreException {

        runningExecutions.put(buildExecutionTask.getId(), buildExecutionTask);
        Consumer<BuildStatus> onCompleteInternal = (buildStatus) -> {
            log.debug("Removing buildExecutionTask [" + buildExecutionTask.getId() + "] form list of running tasks.");
            runningExecutions.remove(buildExecutionTask.getId());
            onComplete.accept(buildStatus);
        };

        CompletableFuture.supplyAsync(() -> configureRepository(buildExecutionTask), executor)
                .thenApplyAsync(repositoryConfiguration -> setUpEnvironment(buildExecutionTask, repositoryConfiguration), executor)
                .thenComposeAsync(startedEnvironment -> waitForEnvironmentInitialization(buildExecutionTask, startedEnvironment), executor)
                .thenApplyAsync(runningEnvironment -> buildSetUp(buildExecutionTask, runningEnvironment), executor)
                .thenComposeAsync(runningBuild -> waitBuildToComplete(buildExecutionTask, runningBuild), executor)
                .thenApplyAsync(completedBuild -> retrieveBuildDriverResults(buildExecutionTask, completedBuild), executor)
                .thenApplyAsync(buildDriverResult -> retrieveRepositoryManagerResults(buildExecutionTask, buildDriverResult), executor)
                .thenApplyAsync(buildResults -> destroyEnvironment(buildExecutionTask, buildResults), executor)
                .handleAsync((buildResults, e) -> storeResults(buildExecutionTask, buildResults, onCompleteInternal, e), executor); //TODO move store results out of executor
    }

    public BuildExecutionTask getRunningExecution(int buildExecutionTaskId) {
        return runningExecutions.get(buildExecutionTaskId);
    }

    private RepositorySession configureRepository(BuildExecutionTask buildExecutionTask) {
        buildExecutionTask.setStatus(BuildStatus.REPO_SETTING_UP);
        try {
            RepositoryManager repositoryManager = repositoryManagerFactory.getRepositoryManager(RepositoryType.MAVEN);
            return repositoryManager.createBuildRepository(buildExecutionTask);
        } catch (Throwable e) {
            throw new BuildProcessException(e);
        }
    }

    private StartedEnvironment setUpEnvironment(BuildExecutionTask buildExecutionTask, RepositorySession repositorySession) {
        buildExecutionTask.setStatus(BuildStatus.BUILD_ENV_SETTING_UP);
        try {
            EnvironmentDriver envDriver = environmentDriverFactory.getDriver(buildExecutionTask.getBuildConfigurationAudited().getBuildEnvironment());
            StartedEnvironment startedEnv = envDriver.buildEnvironment(
                    buildExecutionTask.getBuildConfigurationAudited().getBuildEnvironment(), repositorySession);
            return startedEnv;
        } catch (Throwable e) {
            throw new BuildProcessException(e);
        }
    }

    private CompletableFuture<RunningEnvironment> waitForEnvironmentInitialization(BuildExecutionTask buildExecutionTask, StartedEnvironment startedEnvironment) {
        CompletableFuture<RunningEnvironment> waitToCompleteFuture = new CompletableFuture<>();
        try {
            Consumer<RunningEnvironment> onComplete = (runningEnvironment) -> {
                buildExecutionTask.setStatus(BuildStatus.BUILD_ENV_SETUP_COMPLETE_SUCCESS);
                waitToCompleteFuture.complete(runningEnvironment);
            };
            Consumer<Exception> onError = (e) -> {
                buildExecutionTask.setStatus(BuildStatus.BUILD_ENV_SETUP_COMPLETE_WITH_ERROR);
                waitToCompleteFuture.completeExceptionally(new BuildProcessException(e, startedEnvironment));
            };
            buildExecutionTask.setStatus(BuildStatus.BUILD_ENV_WAITING);

            startedEnvironment.monitorInitialization(onComplete, onError);
        } catch (Throwable e) {
            waitToCompleteFuture.completeExceptionally(new BuildProcessException(e, startedEnvironment));
        }
        return waitToCompleteFuture;
    }

    private RunningBuild buildSetUp(BuildExecutionTask buildExecutionTask, RunningEnvironment runningEnvironment) {
        buildExecutionTask.setStatus(BuildStatus.BUILD_SETTING_UP);
        try {
            buildExecutionTask.setStartTime(new Date());
            BuildDriver buildDriver = buildDriverFactory.getBuildDriver(buildExecutionTask.getBuildConfigurationAudited().getBuildEnvironment().getBuildType());
            return buildDriver.startProjectBuild(buildExecutionTask, buildExecutionTask.getBuildConfigurationAudited(), runningEnvironment);
        } catch (Throwable e) {
            throw new BuildProcessException(e, runningEnvironment);
        }
    }

    private CompletableFuture<CompletedBuild> waitBuildToComplete(BuildExecutionTask buildExecutionTask, RunningBuild runningBuild) {
        CompletableFuture<CompletedBuild> waitToCompleteFuture = new CompletableFuture<>();
        try {
            Consumer<CompletedBuild> onComplete = (completedBuild) -> {
                waitToCompleteFuture.complete(completedBuild);
            };
            Consumer<Throwable> onError = (e) -> {
                waitToCompleteFuture.completeExceptionally(new BuildProcessException(e, runningBuild.getRunningEnvironment()));
            };

            buildExecutionTask.setStatus(BuildStatus.BUILD_WAITING);

            runningBuild.monitor(onComplete, onError);
        } catch (Throwable exception) {
            waitToCompleteFuture.completeExceptionally(
                    new BuildProcessException(exception, runningBuild.getRunningEnvironment()));
        }
        return waitToCompleteFuture;
    }

    private BuildDriverResult retrieveBuildDriverResults(BuildExecutionTask buildExecutionTask, CompletedBuild completedBuild) {
        buildExecutionTask.setEndTime(new Date());
        buildExecutionTask.setStatus(BuildStatus.COLLECTING_RESULTS_FROM_BUILD_DRIVER);
        try {
            BuildDriverResult buildResult = completedBuild.getBuildResult();
            BuildDriverStatus buildDriverStatus = buildResult.getBuildDriverStatus();
            if (buildDriverStatus.completedSuccessfully()) {
                buildExecutionTask.setStatus(BuildStatus.BUILD_COMPLETED_SUCCESS);
            } else {
                buildExecutionTask.setStatus(BuildStatus.BUILD_COMPLETED_WITH_ERROR);
            }
            return buildResult;
        } catch (Throwable e) {
            throw new BuildProcessException(e, completedBuild.getRunningEnvironment());
        }
    }

    private BuildResult retrieveRepositoryManagerResults(BuildExecutionTask buildExecutionTask, BuildDriverResult buildDriverResult) {
        try {
            buildExecutionTask.setStatus(BuildStatus.COLLECTING_RESULTS_FROM_REPOSITORY_NAMAGER);
            RunningEnvironment runningEnvironment  = buildDriverResult.getRunningEnvironment();
            if (BuildDriverStatus.SUCCESS.equals(buildDriverResult.getBuildDriverStatus())) {
                RepositoryManagerResult repositoryManagerResult = runningEnvironment.getRepositorySession().extractBuildArtifacts();
                return new DefaultBuildResult(runningEnvironment, buildDriverResult, repositoryManagerResult);
            } else {
                return new DefaultBuildResult(runningEnvironment, buildDriverResult, null);
            }
        } catch (Throwable e) {
            throw new BuildProcessException(e, buildDriverResult.getRunningEnvironment());
        }
    }

    private BuildResult destroyEnvironment(BuildExecutionTask buildExecutionTask, BuildResult buildResult ) {
        try {
            buildExecutionTask.setStatus(BuildStatus.BUILD_ENV_DESTROYING);
            buildResult.getRunningEnvironment().destroyEnvironment();
            buildExecutionTask.setStatus(BuildStatus.BUILD_ENV_DESTROYED);
            return buildResult;
        } catch (Throwable e) {
            throw new BuildProcessException(e);
        }
    }

    private Void storeResults(BuildExecutionTask buildExecutionTask, BuildResult buildResult, Consumer<BuildStatus> onComplete, Throwable e) {
        try {
            if (buildResult != null) {
                buildExecutionTask.setStatus(BuildStatus.STORING_RESULTS);
                datastoreAdapter.storeResult(buildExecutionTask, buildResult, buildExecutionTask.getId());
            } else {
                // If there are no build results, then there was a system failure
                // which means the build may not have started.
                if (buildExecutionTask.getStartTime() == null) {
                    buildExecutionTask.setStartTime(new Date());
                }
                stopRunningEnvironment(e);
                if (buildExecutionTask.getEndTime() == null) {
                    buildExecutionTask.setEndTime(new Date());
                }

                datastoreAdapter.storeResult(buildExecutionTask, e);
            }
        } catch (DatastoreException de) {
            log.error("Error storing results of build configuration: " + buildExecutionTask.getId()  + " to datastore.", de);
        }

        if (buildExecutionTask.hasFailed()) {
            buildExecutionTask.setStatus(BuildStatus.DONE_WITH_ERRORS);
        } else {
            buildExecutionTask.setStatus(BuildStatus.DONE);
        }
        onComplete.accept(buildExecutionTask.getStatus());
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
            destroyableEnvironment = bpEx.getDestroyableEnvironmnet();
        } else if(ex.getCause() instanceof BuildProcessException) {
            BuildProcessException bpEx = (BuildProcessException) ex.getCause();
            destroyableEnvironment = bpEx.getDestroyableEnvironmnet();
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

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}
