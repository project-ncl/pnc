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
package org.jboss.pnc.core.builder;

import org.jboss.pnc.common.util.ResultWrapper;
import org.jboss.pnc.common.util.StreamCollectors;
import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.EnvironmentDriverFactory;
import org.jboss.pnc.core.RepositoryManagerFactory;
import org.jboss.pnc.core.content.ContentIdentityManager;
import org.jboss.pnc.core.exception.BuildProcessException;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.*;
import org.jboss.pnc.spi.*;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.builddriver.*;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.environment.DestroyableEnvironment;
import org.jboss.pnc.spi.environment.EnvironmentDriver;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.environment.StartedEnvironment;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-20.
 */
@ApplicationScoped
public class BuildCoordinator {

    private Logger log = LoggerFactory.getLogger(BuildCoordinator.class);
    private Queue<BuildTask> buildTasks = new ConcurrentLinkedQueue<>(); //TODO garbage collector (time-out, error state)

//    @Resource
//    private ManagedThreadFactory threadFactory;
    private ExecutorService executor = Executors.newFixedThreadPool(4); //TODO configurable
    //TODO override executor and implement "protected void afterExecute(Runnable r, Throwable t) { }" to catch possible exceptions

    private ExecutorService dbexecutorSingleThread = Executors.newFixedThreadPool(1);

    private RepositoryManagerFactory repositoryManagerFactory;
    private BuildDriverFactory buildDriverFactory;
    private EnvironmentDriverFactory environmentDriverFactory;
    private DatastoreAdapter datastoreAdapter;
    private Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier;
    private Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier;

    @Deprecated
    public BuildCoordinator(){} //workaround for CDI constructor parameter injection

    @Inject
    public BuildCoordinator(BuildDriverFactory buildDriverFactory, RepositoryManagerFactory repositoryManagerFactory,
                            EnvironmentDriverFactory environmentDriverFactory, DatastoreAdapter datastoreAdapter,
                            Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier,
                            Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier) {
        this.buildDriverFactory = buildDriverFactory;
        this.repositoryManagerFactory = repositoryManagerFactory;
        this.datastoreAdapter = datastoreAdapter;
        this.environmentDriverFactory = environmentDriverFactory;
        this.buildStatusChangedEventNotifier = buildStatusChangedEventNotifier;
        this.buildSetStatusChangedEventNotifier = buildSetStatusChangedEventNotifier;
    }

    public BuildTask build(BuildConfiguration buildConfiguration, User userTriggeredBuild) throws CoreException {
        BuildConfigurationSet buildConfigurationSet = BuildConfigurationSet.Builder.newBuilder()
                .name(buildConfiguration.getName())
                .buildConfiguration(buildConfiguration)
                .build();

        BuildSetTask buildSetTask = createBuildSetTask(buildConfigurationSet, userTriggeredBuild, BuildExecutionType.STANDALONE_BUILD);

        build(buildSetTask);
        BuildTask buildTask = buildSetTask.getBuildTasks().stream().collect(StreamCollectors.singletonCollector());
        return buildTask;
    }

    public BuildSetTask build(BuildConfigurationSet buildConfigurationSet, User userTriggeredBuild) throws CoreException, DatastoreException {

        BuildSetTask buildSetTask = createBuildSetTask(buildConfigurationSet, userTriggeredBuild, BuildExecutionType.COMPOSED_BUILD);

        build(buildSetTask);
        return buildSetTask;
    }

    public BuildSetTask createBuildSetTask(BuildConfigurationSet buildConfigurationSet, User user, BuildExecutionType buildType) throws CoreException {
        BuildConfigSetRecord buildConfigSetRecord = BuildConfigSetRecord.Builder.newBuilder()
                .buildConfigurationSet(buildConfigurationSet)
                .user(user)
                .startTime(new Date())
                .status(org.jboss.pnc.model.BuildStatus.BUILDING)
                .build();

        if (BuildExecutionType.COMPOSED_BUILD.equals(buildType)) {
            buildConfigSetRecord = this.saveBuildConfigSetRecord(buildConfigSetRecord);
        }

        BuildSetTask buildSetTask = new BuildSetTask(
                this,
                buildConfigSetRecord,
                buildType,
                getProductMilestone(buildConfigurationSet));

        initializeBuildTasksInSet(buildSetTask);
        return buildSetTask;
    }

    /**
     * Creates build tasks and sets up the appropriate dependency relations
     * 
     * @param buildSetTask The build set task which will contain the build tasks.  This must already have
     * initialized the BuildConfigSet, BuildConfigSetRecord, Milestone, etc.
     */
    private void initializeBuildTasksInSet(BuildSetTask buildSetTask) {
        ContentIdentityManager contentIdentityManager = new ContentIdentityManager();
        String topContentId = contentIdentityManager.getProductContentId(buildSetTask.getBuildConfigurationSet().getProductVersion());
        String buildSetContentId = contentIdentityManager.getBuildSetContentId(buildSetTask.getBuildConfigurationSet());

        // Loop to create the build tasks
        for(BuildConfiguration buildConfig : buildSetTask.getBuildConfigurationSet().getBuildConfigurations()) {
            String buildContentId = contentIdentityManager.getBuildContentId(buildConfig);
            BuildTask buildTask = new BuildTask(
                    this,
                    buildConfig,
                    topContentId,
                    buildSetContentId,
                    buildContentId,
                    BuildExecutionType.COMPOSED_BUILD,
                    buildSetTask.getBuildConfigSetRecord().getUser(),
                    buildSetTask,
                    datastoreAdapter.getNextBuildRecordId());
            buildSetTask.addBuildTask(buildTask);
        }
        // Loop again to set dependencies
        for (BuildTask buildTask : buildSetTask.getBuildTasks()) {
            for (BuildConfiguration dep : buildTask.getBuildConfiguration().getDependencies()) {
                if (buildSetTask.getBuildConfigurationSet().getBuildConfigurations().contains(dep)) {
                    BuildTask depTask = buildSetTask.getBuildTask(dep);
                    if (depTask != null) {
                        buildTask.addDependency(depTask);
                    }
                }
            }
        }
    }

    /**
     * Get the product milestone (if any) associated with this build config set.
     * @param buildConfigSet
     * @return The product milestone, or null if there is none
     */
    private ProductMilestone getProductMilestone(BuildConfigurationSet buildConfigSet) {
        if(buildConfigSet.getProductVersion() == null || buildConfigSet.getProductVersion().getCurrentProductMilestone() == null) {
            return null;
        }
        return buildConfigSet.getProductVersion().getCurrentProductMilestone();
    }

    private void build(BuildSetTask buildSetTask) throws CoreException {


        Predicate<BuildTask> readyToBuild = (buildTask) -> {
            return buildTask.readyToBuild();
        };

        Predicate<BuildTask> rejectAlreadySubmitted = (buildTask) -> {
            if (isBuildAlreadySubmitted(buildTask)) {
                buildTask.setStatus(BuildStatus.REJECTED);
                buildTask.setStatusDescription("The configuration is already in the build queue.");
                return false;
            } else {
                return true;
            }
        };

        if (!BuildSetStatus.REJECTED.equals(buildSetTask.getStatus())) {
            buildSetTask.getBuildTasks().stream()
                    .filter(readyToBuild)
                    .filter(rejectAlreadySubmitted)
                    .forEach(v -> processBuildTask(v));
        }

    }

    private void processBuildTask(BuildTask buildTask) {
        try {
            startBuilding(buildTask);
            buildTasks.add(buildTask);
        } catch (CoreException e) {
            buildTask.setStatus(BuildStatus.SYSTEM_ERROR);
            buildTask.setStatusDescription(e.getMessage());
        }
    }

    private boolean isConfigurationBuilt(BuildConfiguration buildConfiguration) {
        return datastoreAdapter.isBuildConfigurationBuilt();
    }

    void startBuilding(BuildTask buildTask) throws CoreException {
        CompletableFuture.supplyAsync(() -> configureRepository(buildTask), executor)
            .thenApplyAsync(repositoryConfiguration -> setUpEnvironment(buildTask, repositoryConfiguration), executor)
            .thenComposeAsync(startedEnvironment -> waitForEnvironmentInitialization(buildTask, startedEnvironment), executor)
            .thenApplyAsync(runningEnvironment -> buildSetUp(buildTask, runningEnvironment), executor)
            .thenComposeAsync(runningBuild -> waitBuildToComplete(buildTask, runningBuild), executor)
            .thenApplyAsync(completedBuild -> retrieveBuildDriverResults(buildTask, completedBuild), executor)
            .thenApplyAsync(buildDriverResult -> retrieveRepositoryManagerResults(buildTask, buildDriverResult), executor)
            .thenApplyAsync(buildResults -> destroyEnvironment(buildTask, buildResults), executor)
            .handleAsync((buildResults, e) -> storeResults(buildTask, buildResults, e), executor);
    }

    private RepositorySession configureRepository(BuildTask buildTask) {
        buildTask.setStatus(BuildStatus.REPO_SETTING_UP);
        try {
            RepositoryManager repositoryManager = repositoryManagerFactory.getRepositoryManager(RepositoryType.MAVEN);
            return repositoryManager.createBuildRepository(buildTask);
        } catch (Throwable e) {
            throw new BuildProcessException(e);
        }
    }

    private StartedEnvironment setUpEnvironment(BuildTask buildTask, RepositorySession repositorySession) {
            buildTask.setStatus(BuildStatus.BUILD_ENV_SETTING_UP);
            try {
                EnvironmentDriver envDriver = environmentDriverFactory.getDriver(buildTask.getBuildConfiguration().getEnvironment());
                StartedEnvironment startedEnv = envDriver.buildEnvironment(
                        buildTask.getBuildConfiguration().getEnvironment(), repositorySession);
                return startedEnv;
            } catch (Throwable e) {
                throw new BuildProcessException(e);
            }
    }
    
    private CompletableFuture<RunningEnvironment> waitForEnvironmentInitialization(BuildTask buildTask, StartedEnvironment startedEnvironment) {
        CompletableFuture<RunningEnvironment> waitToCompleteFuture = new CompletableFuture<>();
            try {
                Consumer<RunningEnvironment> onComplete = (runningEnvironment) -> {
                    buildTask.setStatus(BuildStatus.BUILD_ENV_SETUP_COMPLETE_SUCCESS);
                    waitToCompleteFuture.complete(runningEnvironment);
                };
                Consumer<Exception> onError = (e) -> {
                    buildTask.setStatus(BuildStatus.BUILD_ENV_SETUP_COMPLETE_WITH_ERROR);
                    waitToCompleteFuture.completeExceptionally(new BuildProcessException(e, startedEnvironment));
                };
                buildTask.setStatus(BuildStatus.BUILD_ENV_WAITING);

                startedEnvironment.monitorInitialization(onComplete, onError);
            } catch (Throwable e) {
                waitToCompleteFuture.completeExceptionally(new BuildProcessException(e, startedEnvironment));
            }
            return waitToCompleteFuture;
    }

    private RunningBuild buildSetUp(BuildTask buildTask, RunningEnvironment runningEnvironment) {
        buildTask.setStatus(BuildStatus.BUILD_SETTING_UP);
        try {
            BuildDriver buildDriver = buildDriverFactory.getBuildDriver(buildTask.getBuildConfiguration().getEnvironment().getBuildType());
            return buildDriver.startProjectBuild(buildTask, buildTask.getBuildConfiguration(), runningEnvironment);
        } catch (Throwable e) {
            throw new BuildProcessException(e, runningEnvironment);
        }
    }
    
    private CompletableFuture<CompletedBuild> waitBuildToComplete(BuildTask buildTask, RunningBuild runningBuild) {
        CompletableFuture<CompletedBuild> waitToCompleteFuture = new CompletableFuture<>();
            try {
                Consumer<CompletedBuild> onComplete = (completedBuild) -> {
                    waitToCompleteFuture.complete(completedBuild);
                };
                Consumer<Throwable> onError = (e) -> {
                    waitToCompleteFuture.completeExceptionally(new BuildProcessException(e, runningBuild.getRunningEnvironment()));
                };
                
                buildTask.setStatus(BuildStatus.BUILD_WAITING);

                runningBuild.monitor(onComplete, onError);
            } catch (Throwable exception) {
                waitToCompleteFuture.completeExceptionally( 
                        new BuildProcessException(exception, runningBuild.getRunningEnvironment()));
            }
        return waitToCompleteFuture;
    }

    private BuildDriverResult retrieveBuildDriverResults(BuildTask buildTask, CompletedBuild completedBuild) {
        buildTask.setStatus(BuildStatus.COLLECTING_RESULTS_FROM_BUILD_DRIVER);
        try {
            BuildDriverResult buildResult = completedBuild.getBuildResult();
            BuildDriverStatus buildDriverStatus = buildResult.getBuildDriverStatus();
            if (buildDriverStatus.completedSuccessfully()) {
                buildTask.setStatus(BuildStatus.BUILD_COMPLETED_SUCCESS);
            } else {
                buildTask.setStatus(BuildStatus.BUILD_COMPLETED_WITH_ERROR);
            }
            return buildResult;
        } catch (Throwable e) {
            throw new BuildProcessException(e, completedBuild.getRunningEnvironment());
        }
    }

    private BuildResult retrieveRepositoryManagerResults(BuildTask buildTask, BuildDriverResult buildDriverResult) {
        try {
            buildTask.setStatus(BuildStatus.COLLECTING_RESULTS_FROM_REPOSITORY_NAMAGER);
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

    private BuildResult destroyEnvironment(BuildTask buildTask, BuildResult buildResult ) {
        try {
            buildTask.setStatus(BuildStatus.BUILD_ENV_DESTROYING);
            buildResult.getRunningEnvironment().destroyEnvironment();
            buildTask.setStatus(BuildStatus.BUILD_ENV_DESTROYED);
            return buildResult;
        } catch (Throwable e) {
            throw new BuildProcessException(e);
        }
    }
    
    private BuildRecord storeResults(BuildTask buildTask, BuildResult buildResult, Throwable e) {
        BuildRecord storedBuildRecord = null;
        try {
            if (buildResult != null) {
                buildTask.setStatus(BuildStatus.STORING_RESULTS);
                storedBuildRecord = datastoreAdapter.storeResult(buildTask, buildResult, buildTask.getId());
            } else {
                stopRunningEnvironment(e);
                datastoreAdapter.storeResult(buildTask, e);
            }
        } catch (DatastoreException de) {
            log.error("Error storing results of build configuration: " + buildTask.getId()  + " to datastore.", de);
        }

        if (buildTask.hasFailed())
            buildTask.setStatus(BuildStatus.DONE_WITH_ERRORS);
        else
            buildTask.setStatus(BuildStatus.DONE);

        buildTasks.remove(buildTask);
        return storedBuildRecord;
    }

    /**
     * Save the build config set record using a single thread for all db operations.
     * This ensures that database operations are done in the correct sequence, for example
     * in the case of a build config set.
     *
     * @param buildConfigSetRecord
     * @return
     */
    public BuildConfigSetRecord saveBuildConfigSetRecord(BuildConfigSetRecord buildConfigSetRecord) throws CoreException {
        ResultWrapper<BuildConfigSetRecord, DatastoreException> result = null;
        try {
            result = CompletableFuture.supplyAsync(() -> this.saveBuildConfigSetRecordInternal(buildConfigSetRecord), dbexecutorSingleThread).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new CoreException(e);
        }
        if (result.getException() != null) {
            throw new CoreException(result.getException());
        } else {
            return result.getResult();
        }
    }

    /**
     * Save the build config set record to the database.  The result wrapper will contain an exception
     * if there is a problem while saving.
     */
    private ResultWrapper<BuildConfigSetRecord, DatastoreException> saveBuildConfigSetRecordInternal(
            BuildConfigSetRecord buildConfigSetRecord) {

        try {
            buildConfigSetRecord = datastoreAdapter.saveBuildConfigSetRecord(buildConfigSetRecord);
            return new ResultWrapper<>(buildConfigSetRecord);
        } catch (DatastoreException e) {
            log.error("Unable to save build config set record", e);
            return new ResultWrapper<>(buildConfigSetRecord, e);
        }
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

    public List<BuildTask> getBuildTasks() {
        return Collections.unmodifiableList(buildTasks.stream().collect(Collectors.toList()));
    }

    private boolean isBuildAlreadySubmitted(BuildTask buildTask) {
        return buildTasks.contains(buildTask);
    }

    Event<BuildStatusChangedEvent> getBuildStatusChangedEventNotifier() {
        return buildStatusChangedEventNotifier;
    }

    Event<BuildSetStatusChangedEvent> getBuildSetStatusChangedEventNotifier() {
        return buildSetStatusChangedEventNotifier;
    }

    public void shutdownCoordinator(){
        executor.shutdown();
    }
}
