package org.jboss.pnc.core.builder;

import org.jboss.logging.Logger;
import org.jboss.pnc.common.util.StreamCollectors;
import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.EnvironmentDriverFactory;
import org.jboss.pnc.core.RepositoryManagerFactory;
import org.jboss.pnc.core.content.ContentIdentityManager;
import org.jboss.pnc.core.exception.BuildProcessException;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildExecutionType;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.BuildDriverStatus;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.environment.DestroyableEnvironmnet;
import org.jboss.pnc.spi.environment.EnvironmentDriver;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.environment.StartedEnvironment;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.util.graph.Edge;
import org.jboss.util.graph.Vertex;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
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

    private Logger log = Logger.getLogger(BuildCoordinator.class);
    private Queue<BuildTask> buildTasks = new ConcurrentLinkedQueue<>(); //TODO garbage collector (time-out, error state)

//    @Resource
//    private ManagedThreadFactory threadFactory;
    private Executor executor = Executors.newFixedThreadPool(4); //TODO configurable
    //TODO override executor and implement "protected void afterExecute(Runnable r, Throwable t) { }" to catch possible exceptions

    private RepositoryManagerFactory repositoryManagerFactory;
    private BuildDriverFactory buildDriverFactory;
    private EnvironmentDriverFactory environmentDriverFactory;
    private DatastoreAdapter datastoreAdapter;
    private Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier;
    private ContentIdentityManager contentIdentityManager;

    @Deprecated
    public BuildCoordinator(){} //workaround for CDI constructor parameter injection

    @Inject
    public BuildCoordinator(BuildDriverFactory buildDriverFactory, RepositoryManagerFactory repositoryManagerFactory,
                            EnvironmentDriverFactory environmentDriverFactory, DatastoreAdapter datastoreAdapter,
                            Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier,
                            ContentIdentityManager contentIdentityManager) {
        this.buildDriverFactory = buildDriverFactory;
        this.repositoryManagerFactory = repositoryManagerFactory;
        this.datastoreAdapter = datastoreAdapter;
        this.environmentDriverFactory = environmentDriverFactory;
        this.buildStatusChangedEventNotifier = buildStatusChangedEventNotifier;
        this.contentIdentityManager = contentIdentityManager;
    }

    public BuildTask build(BuildConfiguration buildConfiguration) throws CoreException {
        BuildConfigurationSet buildConfigurationSet = new BuildConfigurationSet();
        buildConfigurationSet.setName(buildConfiguration.getName());
        buildConfigurationSet.addBuildConfiguration(buildConfiguration);
        BuildSetTask buildSetTask = new BuildSetTask(buildConfigurationSet, BuildExecutionType.STANDALONE_BUILD);
        build(buildSetTask);
        BuildTask buildTask = buildSetTask.getBuildTasks().stream().collect(StreamCollectors.singletonCollector());
        return buildTask;
    }

    public BuildSetTask build(BuildConfigurationSet buildConfigurationSet) throws CoreException {
        BuildSetTask buildSetTask = new BuildSetTask(buildConfigurationSet, BuildExecutionType.COMPOSED_BUILD);
        build(buildSetTask);
        return buildSetTask;
    }

    private void build(BuildSetTask buildSetTask) throws CoreException {

        User user = null; //TODO user
        BuildTasksTree buildTasksTree = BuildTasksTree.newInstance(this, buildSetTask, user);

        Predicate<Vertex<BuildTask>> acceptOnlyStatus = (vertex) -> {
            BuildTask build = vertex.getData();
            List<BuildStatus> buildStatuses = Arrays.asList(BuildStatus.NEW, BuildStatus.WAITING_FOR_DEPENDENCIES);
            return buildStatuses.contains(build.getStatus());
        };

        Predicate<Vertex<BuildTask>> rejectAlreadySubmitted = (vertex) -> {
            BuildTask build = vertex.getData();
            if (isBuildAlreadySubmitted(build)) {
                build.setStatus(BuildStatus.REJECTED);
                build.setStatusDescription("The configuration is already in the build queue.");
                return false;
            } else {
                return true;
            }
        };

        if (!BuildStatus.REJECTED.equals(buildSetTask.getStatus())) {
            buildTasksTree.getBuildTasks().stream()
                    .filter(acceptOnlyStatus)
                    .filter(rejectAlreadySubmitted)
                    .forEach(v -> processBuildTask(v));
        }
    }

    private void processBuildTask(Vertex<BuildTask> vertex) {
        BuildTask buildTask = vertex.getData();
        List<BuildTask> missingDependencies = findDirectMissingDependencies(vertex);
        missingDependencies.forEach((missingDependency) -> missingDependency.addWaiting(buildTask));
        if (missingDependencies.size() == 0) {
            try {
                startBuilding(buildTask);
                buildTasks.add(buildTask);
            } catch (CoreException e) {
                buildTask.setStatus(BuildStatus.SYSTEM_ERROR);
                buildTask.setStatusDescription(e.getMessage());
            }
        } else {
            buildTask.setStatus(BuildStatus.WAITING_FOR_DEPENDENCIES);
            buildTask.setRequiredBuilds(missingDependencies);
            buildTasks.add(buildTask);
        }
    }

    private List<BuildTask> findDirectMissingDependencies(Vertex<BuildTask> vertex) {
        List<BuildTask> missingDependencies = new ArrayList<>();
        List<Edge<BuildTask>> outgoingEdges = vertex.getOutgoingEdges();
        for (Edge<BuildTask> outgoingEdge : outgoingEdges) {
            Vertex<BuildTask> dependentVertex = outgoingEdge.getTo();
            BuildTask dependentBuildTask = dependentVertex.getData();
            if (!isConfigurationBuilt(dependentBuildTask.buildConfiguration)) {
                missingDependencies.add(dependentBuildTask);
            }
        }
        return missingDependencies;
    }

    private boolean isConfigurationBuilt(BuildConfiguration buildConfiguration) {
        return datastoreAdapter.isBuildConfigurationBuilt();
    }

    void startBuilding(BuildTask buildTask) throws CoreException {
        configureRepository(buildTask)
            .thenCompose(repositoryConfiguration -> setUpEnvironment(buildTask, repositoryConfiguration))
            .thenCompose(startedEnvironment -> waitForEnvironmentInitialization(buildTask, startedEnvironment))
            .thenCompose(runningEnvironment -> buildSetUp(buildTask, runningEnvironment))
            .thenCompose(runningBuild -> waitBuildToComplete(buildTask, runningBuild))
            .thenCompose(completedBuild -> retrieveBuildDriverResults(buildTask, completedBuild))
            .thenCompose(buildDriverResult -> retrieveRepositoryManagerResults(buildTask, buildDriverResult))
            .thenCompose(buildResults -> destroyEnvironment(buildTask, buildResults))
            .handle((buildResults, e) -> storeResults(buildTask, buildResults, e));
    }

    private CompletableFuture<RepositorySession> configureRepository(BuildTask buildTask) {
        return CompletableFuture.supplyAsync( () ->  {
            buildTask.setStatus(BuildStatus.REPO_SETTING_UP);
            try {
                RepositoryManager repositoryManager = repositoryManagerFactory.getRepositoryManager(RepositoryType.MAVEN);
                return repositoryManager.createBuildRepository(buildTask);
            } catch (Throwable e) {
                throw new BuildProcessException(e);
            }
        }, executor);
    }

    private CompletableFuture<StartedEnvironment> setUpEnvironment(BuildTask buildTask, RepositorySession repositorySession) {
            return CompletableFuture.supplyAsync(() -> {
                buildTask.setStatus(BuildStatus.BUILD_ENV_SETTING_UP);
                try {
                    EnvironmentDriver envDriver = environmentDriverFactory.getDriver(buildTask.getBuildConfiguration().getEnvironment());
                    StartedEnvironment startedEnv = envDriver.buildEnvironment(
                            buildTask.getBuildConfiguration().getEnvironment(), repositorySession);
                    return startedEnv;
                } catch (Throwable e) {
                    throw new BuildProcessException(e);
                }
            }, executor);
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
                    waitToCompleteFuture.completeExceptionally(e);
                };
                buildTask.setStatus(BuildStatus.BUILD_ENV_WAITING);

                startedEnvironment.monitorInitialization(onComplete, onError);
            } catch (Throwable e) {
                waitToCompleteFuture.completeExceptionally(new BuildProcessException(e, startedEnvironment));
            }
            return waitToCompleteFuture;
    }

    private CompletableFuture<RunningBuild> buildSetUp(BuildTask buildTask, RunningEnvironment runningEnvironment) {
        return CompletableFuture.supplyAsync(() -> {
            buildTask.setStatus(BuildStatus.BUILD_SETTING_UP);
            try {
                BuildDriver buildDriver = buildDriverFactory.getBuildDriver(buildTask.getBuildConfiguration().getEnvironment().getBuildType());
                return buildDriver.startProjectBuild(buildTask.getBuildConfiguration(), runningEnvironment);
            } catch (Throwable e) {
                throw new BuildProcessException(e, runningEnvironment);
            }
        }, executor);
    }

    private CompletableFuture<CompletedBuild> waitBuildToComplete(BuildTask buildTask, RunningBuild runningBuild) {
        CompletableFuture<CompletedBuild> waitToCompleteFuture = new CompletableFuture<>();
            try {
                Consumer<CompletedBuild> onComplete = (completedBuild) -> {
                    waitToCompleteFuture.complete(completedBuild);
                };
                Consumer<Exception> onError = (e) -> {
                    waitToCompleteFuture.completeExceptionally(e);
                };
                buildTask.setStatus(BuildStatus.BUILD_WAITING);

                runningBuild.monitor(onComplete, onError);
            } catch (Throwable exception) {
                waitToCompleteFuture.completeExceptionally(
                        new BuildProcessException(exception, runningBuild.getRunningEnvironment()));
            }
        return waitToCompleteFuture;
    }

    private CompletionStage<BuildDriverResult> retrieveBuildDriverResults(BuildTask buildTask, CompletedBuild completedBuild) {
        return CompletableFuture.supplyAsync(() -> {
            buildTask.setStatus(BuildStatus.COLLECTING_RESULTS_FROM_BUILD_DRIVER);
            try {
                BuildDriverResult buildResult = completedBuild.getBuildResult();
                BuildDriverStatus buildDriverStatus = buildResult.getBuildDriverStatus();
                if (buildDriverStatus == BuildDriverStatus.SUCCESS) {
                    buildTask.setStatus(BuildStatus.BUILD_COMPLETED_SUCCESS);
                } else {
                    buildTask.setStatus(BuildStatus.BUILD_COMPLETED_WITH_ERROR);
                }
                return buildResult;
            } catch (Throwable e) {
                throw new BuildProcessException(e, completedBuild.getRunningEnvironment());
            }
        }, executor);
    }

    private CompletionStage<BuildResult> retrieveRepositoryManagerResults(BuildTask buildTask, BuildDriverResult buildDriverResult) {
        return CompletableFuture.supplyAsync( () ->  {
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
        }, executor);
    }

    private CompletableFuture<BuildResult> destroyEnvironment(BuildTask buildTask, BuildResult buildResult ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                buildTask.setStatus(BuildStatus.BUILD_ENV_DESTROYING);
                buildResult.getRunningEnvironment().destroyEnvironment();
                buildTask.setStatus(BuildStatus.BUILD_ENV_DESTROYED);
                return buildResult;
            } catch (Throwable e) {
                throw new BuildProcessException(e);
            }
        }, executor);
    }
    
    private CompletableFuture<Boolean> storeResults(BuildTask buildTask, BuildResult buildResult, Throwable e) {
        return CompletableFuture.supplyAsync( () -> {
            boolean completedOk = false;
            try {
                if (buildResult != null) {
                    buildTask.setStatus(BuildStatus.STORING_RESULTS);
                    datastoreAdapter.storeResult(buildTask, buildResult);
                    completedOk = true;
                } else {
                    stopRunningEnvironment(e);
                    datastoreAdapter.storeResult(buildTask, e);
                    completedOk = false;
                }
            } catch (DatastoreException de) {
                log.errorf(e, "Error storing results of build configuration: %s to datastore.", buildTask.getId());
            }
            finally {
                buildTask.setStatus(BuildStatus.DONE);
                buildTasks.remove(buildTask);
            }
            return completedOk;
        }, executor);
    }

    /**
     * Tries to stop running environment if the exception contains information about running environment
     * 
     * @param ex Exception in build process (To stop the environment it has to be instance of BuildProcessException)
     */
    private void stopRunningEnvironment(Throwable ex) {
        DestroyableEnvironmnet destroyableEnvironmnet = null;
        if(ex instanceof BuildProcessException) {
            BuildProcessException bpEx = (BuildProcessException) ex;
            destroyableEnvironmnet = bpEx.getDestroyableEnvironmnet();
        } else if(ex.getCause() instanceof BuildProcessException) {
            BuildProcessException bpEx = (BuildProcessException) ex.getCause();
            destroyableEnvironmnet = bpEx.getDestroyableEnvironmnet();
        } else {
            //It shouldn't never happen - Throwable should be caught in all steps of build chain
            //and BuildProcessException should be thrown instead of that
            log.warn("Possible leak of a running environment! Build process ended with exception, "
                    + "but the exception didn't contain information about running environment.", ex);
        }
        try {
            if (destroyableEnvironmnet != null) {
                destroyableEnvironmnet.destroyEnvironment();
            }
        } catch (EnvironmentDriverException envE) {
            log.warn("Running environment" + destroyableEnvironmnet + " couldn't be destroyed!", envE);
        }
    }

    public List<BuildTask> getBuildTasks() {
        return Collections.unmodifiableList(buildTasks.stream().collect(Collectors.toList()));
    }

    public BuildTask getBuild(String identifier) {
        List<BuildTask> buildsFilteredTask = buildTasks.stream()
                .filter(b -> identifier.equals(b.buildConfiguration.getName())).collect(Collectors.toList());
        return buildsFilteredTask.get(0); //TODO validate that there is exactly one ?
    }

    private boolean isBuildAlreadySubmitted(BuildTask buildTask) {
        return buildTasks.contains(buildTask);
    }

    Event<BuildStatusChangedEvent> getBuildStatusChangedEventNotifier() {
        return buildStatusChangedEventNotifier;
    }

}
