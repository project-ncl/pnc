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

import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.EnvironmentDriverFactory;
import org.jboss.pnc.core.RepositoryManagerFactory;
import org.jboss.pnc.core.exception.BuildProcessException;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.RepositoryType;
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
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildExecutor {

    private Logger log = LoggerFactory.getLogger(BuildExecutor.class);

//    @Resource
//    private ManagedThreadFactory threadFactory;
    private ExecutorService executor = Executors.newFixedThreadPool(4); //TODO configurable
    //TODO override executor and implement "protected void afterExecute(Runnable r, Throwable t) { }" to catch possible exceptions

    private DatastoreAdapter datastoreAdapter;
    private RepositoryManagerFactory repositoryManagerFactory;
    private BuildDriverFactory buildDriverFactory;
    private EnvironmentDriverFactory environmentDriverFactory;


    @Deprecated
    public BuildExecutor() {}; //CDI workaround for constructor injection

    @Inject
    public BuildExecutor(DatastoreAdapter datastoreAdapter, RepositoryManagerFactory repositoryManagerFactory, BuildDriverFactory buildDriverFactory, EnvironmentDriverFactory environmentDriverFactory) {
        this.datastoreAdapter = datastoreAdapter;
        this.repositoryManagerFactory = repositoryManagerFactory;
        this.buildDriverFactory = buildDriverFactory;
        this.environmentDriverFactory = environmentDriverFactory;
    }

    public void startBuilding(BuildTask buildTask, Runnable onComplete) throws CoreException {
        CompletableFuture.supplyAsync(() -> configureRepository(buildTask), executor)
                .thenApplyAsync(repositoryConfiguration -> setUpEnvironment(buildTask, repositoryConfiguration), executor)
                .thenComposeAsync(startedEnvironment -> waitForEnvironmentInitialization(buildTask, startedEnvironment), executor)
                .thenApplyAsync(runningEnvironment -> buildSetUp(buildTask, runningEnvironment), executor)
                .thenComposeAsync(runningBuild -> waitBuildToComplete(buildTask, runningBuild), executor)
                .thenApplyAsync(completedBuild -> retrieveBuildDriverResults(buildTask, completedBuild), executor)
                .thenApplyAsync(buildDriverResult -> retrieveRepositoryManagerResults(buildTask, buildDriverResult), executor)
                .thenApplyAsync(buildResults -> destroyEnvironment(buildTask, buildResults), executor)
                .handleAsync((buildResults, e) -> storeResults(buildTask, buildResults, onComplete, e), executor);
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
            EnvironmentDriver envDriver = environmentDriverFactory.getDriver(buildTask.getBuildConfigurationAudited().getEnvironment());
            StartedEnvironment startedEnv = envDriver.buildEnvironment(
                    buildTask.getBuildConfigurationAudited().getEnvironment(), repositorySession);
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
            buildTask.setStartTime(new Date());
            BuildDriver buildDriver = buildDriverFactory.getBuildDriver(buildTask.getBuildConfigurationAudited().getEnvironment().getBuildType());
            return buildDriver.startProjectBuild(buildTask, buildTask.getBuildConfigurationAudited(), runningEnvironment);
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
        buildTask.setEndTime(new Date());
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

    private Void storeResults(BuildTask buildTask, BuildResult buildResult, Runnable onComplete, Throwable e) {
        try {
            if (buildResult != null) {
                buildTask.setStatus(BuildStatus.STORING_RESULTS);
                datastoreAdapter.storeResult(buildTask, buildResult, buildTask.getId());
            } else {
                // If there are no build results, then there was a system failure
                // which means the build may not have started.
                if (buildTask.getStartTime() == null) {
                    buildTask.setStartTime(new Date());
                }
                stopRunningEnvironment(e);
                if (buildTask.getEndTime() == null) {
                    buildTask.setEndTime(new Date());
                }

                datastoreAdapter.storeResult(buildTask, e);
            }
        } catch (DatastoreException de) {
            log.error("Error storing results of build configuration: " + buildTask.getId()  + " to datastore.", de);
        }

        if (buildTask.hasFailed()) {
            buildTask.setStatus(BuildStatus.DONE_WITH_ERRORS);
        } else {
            buildTask.setStatus(BuildStatus.DONE);
        }
        onComplete.run();
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
