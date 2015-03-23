package org.jboss.pnc.core.builder;

import org.jboss.logging.Logger;
import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.EnvironmentDriverFactory;
import org.jboss.pnc.core.RepositoryManagerFactory;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.core.exception.CoreExceptionWrapper;
import org.jboss.pnc.model.*;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.environment.EnvironmentDriver;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConfiguration;
import org.jboss.util.graph.Edge;
import org.jboss.util.graph.Vertex;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-20.
 */
@ApplicationScoped
public class BuildCoordinator {

    private Logger log = Logger.getLogger(BuildCoordinator.class);
    private Queue<BuildTask> buildTasks = new ConcurrentLinkedQueue(); //TODO garbage collector (time-out, error state)

//    @Resource
//    private ManagedThreadFactory threadFactory;
    private Executor executor = Executors.newFixedThreadPool(4); //TODO configurable
    //TODO override executor and implement "protected void afterExecute(Runnable r, Throwable t) { }" to catch possible exceptions

    private RepositoryManagerFactory repositoryManagerFactory;
    private BuildDriverFactory buildDriverFactory;
    private EnvironmentDriverFactory environmentDriverFactory;
    private DatastoreAdapter datastoreAdapter;

    private Instance<Consumer<BuildStatusChangedEvent>> registeredEventListeners;

    @Deprecated
    public BuildCoordinator(){} //workaround for CDI constructor parameter injection

    @Inject
    public BuildCoordinator(BuildDriverFactory buildDriverFactory, RepositoryManagerFactory repositoryManagerFactory, 
            EnvironmentDriverFactory environmentDriverFactory, DatastoreAdapter datastoreAdapter, Instance<Consumer<BuildStatusChangedEvent>> registeredEventListeners) {
        this.buildDriverFactory = buildDriverFactory;
        this.repositoryManagerFactory = repositoryManagerFactory;
        this.datastoreAdapter = datastoreAdapter;
        this.environmentDriverFactory = environmentDriverFactory;
        this.registeredEventListeners = registeredEventListeners;
    }

    public BuildTask build(BuildConfiguration buildConfiguration) throws CoreException {
        return build(buildConfiguration, Collections.emptySet(), Collections.emptySet());
    }

    public BuildTask build(BuildConfiguration buildConfiguration, Set<Consumer<BuildStatusChangedEvent>> statusUpdateListeners, Set<Consumer<String>> logConsumers) throws CoreException {
        Set<Consumer<BuildStatusChangedEvent>> aggregatedListOfEventConsumers = createListOfConsumers(statusUpdateListeners);
        BuildTasksTree buildTasksTree = new BuildTasksTree(this);

        BuildTask buildTask = buildTasksTree.getOrCreateSubmittedBuild(buildConfiguration, aggregatedListOfEventConsumers, logConsumers);

        if (!isBuildAlreadySubmitted(buildTask)) {

            Edge<BuildTask>[] cycles = buildTasksTree.findCycles();
            if (cycles.length > 0) {
                buildTask.setStatus(BuildStatus.REJECTED);
                buildTask.setStatusDescription("Cycle dependencies found."); //TODO add edges to description
            }

            Predicate<Vertex<BuildTask>> acceptOnly = (vertex) -> {
                BuildTask build = vertex.getData();
                List<BuildStatus> buildStatuses = Arrays.asList(BuildStatus.NEW, BuildStatus.WAITING_FOR_DEPENDENCIES);
                return buildStatuses.contains(build.getStatus());
            };

            buildTasksTree.getSubmittedBuilds().stream().filter(acceptOnly)
                    .forEach(processBuildTask());
        } else {
            buildTask.setStatus(BuildStatus.REJECTED);
            buildTask.setStatusDescription("The configuration is already in the build queue.");
        }
        return buildTask;
    }

    private Set<Consumer<BuildStatusChangedEvent>> createListOfConsumers(Set<Consumer<BuildStatusChangedEvent>> initialSet) {
        Set<Consumer<BuildStatusChangedEvent>> aggregatedConsumers = new HashSet<>();
        aggregatedConsumers.addAll(initialSet);
        if(registeredEventListeners != null) {
            StreamSupport.stream(registeredEventListeners.spliterator(), false).forEach(listener -> aggregatedConsumers.add(listener));
        }
        return aggregatedConsumers;
    }

    private Consumer<Vertex<BuildTask>> processBuildTask() {
        return (vertex) -> {
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
        };
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
        RepositoryManager repositoryManager = repositoryManagerFactory.getRepositoryManager(RepositoryType.MAVEN);
        BuildDriver buildDriver = buildDriverFactory.getBuildDriver(buildTask.getBuildConfiguration().getEnvironment().getBuildType());
        EnvironmentDriver envDriver = environmentDriverFactory.getDriver(buildTask.getBuildConfiguration().getEnvironment());
        
        RunningEnvironmentWrapper backupRunningEnvironment = new RunningEnvironmentWrapper();
        
        configureRepository(buildTask, repositoryManager)
                .thenCompose(repositoryConfiguration -> setUpEnvironment(buildTask, envDriver, repositoryConfiguration, backupRunningEnvironment))
                .thenCompose(runningEnvironment -> buildSetUp(buildTask, buildDriver, runningEnvironment))
                .thenCompose(runningBuild -> waitBuildToComplete(buildTask, runningBuild))
                .thenCompose(completedBuild -> retrieveBuildDriverResults(buildTask, completedBuild))
                .thenCompose(buildDriverResult -> retrieveRepositoryManagerResults(buildTask, buildDriverResult))
                .thenCompose(buildResults -> destroyEnvironment(buildTask, buildResults))
                .handle((buildResults, e) -> storeResults(buildTask, buildResults, backupRunningEnvironment, e));
    }

    private CompletableFuture<RepositoryConfiguration> configureRepository(BuildTask buildTask, RepositoryManager repositoryManager) {
        return CompletableFuture.supplyAsync( () ->  {
            buildTask.setStatus(BuildStatus.REPO_SETTING_UP);
            BuildConfiguration buildConfiguration = buildTask.getBuildConfiguration();
            try {

                //TODO remove buildRecordSet mock
                BuildRecordSet buildRecordSet = new BuildRecordSet();
                ProductVersion productVersion = new ProductVersion();
                productVersion.setVersion("my-product-version");
                Product product = new Product();
                product.setName("my-product");
                productVersion.setProduct(product);
                buildRecordSet.setProductVersion(productVersion);

                return repositoryManager.createRepository(buildConfiguration, buildRecordSet);
            } catch (RepositoryManagerException e) {
                throw new CoreExceptionWrapper(e);
            }
        }, executor);
    }

    private CompletableFuture<RunningEnvironment> setUpEnvironment(BuildTask buildTask, 
            EnvironmentDriver envDriver, RepositoryConfiguration repositoryConfiguration,
            RunningEnvironmentWrapper backupRunningEnvironment) {
            return CompletableFuture.supplyAsync( () ->  {
                buildTask.setStatus(BuildStatus.BUILD_ENV_SETTING_UP);
                
                try {
                    RunningEnvironment runningEnv = envDriver.buildEnvironment(
                            buildTask.getBuildConfiguration().getEnvironment(), repositoryConfiguration);

                    backupRunningEnvironment.setRunningEnvironment(runningEnv);
                    buildTask.setStatus(BuildStatus.BUILD_ENV_SETUP_COMPLETE_SUCCESS);
                    return runningEnv;
                } catch (EnvironmentDriverException e) {
                    buildTask.setStatus(BuildStatus.BUILD_ENV_SETUP_COMPLETE_WITH_ERROR);
                    throw new CoreExceptionWrapper(e);
                }          
            }, executor);
    }

    private CompletableFuture<RunningBuild> buildSetUp(BuildTask buildTask, BuildDriver buildDriver, RunningEnvironment runningEnvironment) {
        return CompletableFuture.supplyAsync( () ->  {
            buildTask.setStatus(BuildStatus.BUILD_SETTING_UP);
            try {
                return buildDriver.startProjectBuild(buildTask.getBuildConfiguration(), runningEnvironment);
            } catch (BuildDriverException e) {
                throw new CoreExceptionWrapper(e);
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
            } catch (Exception exception) {
                waitToCompleteFuture.completeExceptionally(exception);
            }
        return waitToCompleteFuture;
    }

    private CompletionStage<BuildDriverResult> retrieveBuildDriverResults(BuildTask buildTask, CompletedBuild completedBuild) {
        return CompletableFuture.supplyAsync( () ->  {
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
            } catch (BuildDriverException e) {
                throw new CoreExceptionWrapper(e);
            }
        }, executor);
    }

    private CompletionStage<BuildResult> retrieveRepositoryManagerResults(BuildTask buildTask, BuildDriverResult buildDriverResult) {
        return CompletableFuture.supplyAsync( () ->  {
            try {
                buildTask.setStatus(BuildStatus.COLLECTING_RESULTS_FROM_REPOSITORY_NAMAGER);
                RunningEnvironment runningEnvironment  = buildDriverResult.getRunningEnvironment();
                if (BuildDriverStatus.SUCCESS.equals(buildDriverResult.getBuildDriverStatus())) {
                    RepositoryManagerResult repositoryManagerResult = runningEnvironment.getRepositoryConfiguration().extractBuildArtifacts();
                    return new DefaultBuildResult(runningEnvironment, buildDriverResult, repositoryManagerResult);
                } else {
                    return new DefaultBuildResult(runningEnvironment, buildDriverResult, null);
                }
            } catch (BuildDriverException | RepositoryManagerException e) {
                throw new CoreExceptionWrapper(e);
            }
        }, executor);
    }

    private CompletableFuture<BuildResult> destroyEnvironment(BuildTask buildTask, BuildResult buildResult ) {
        return CompletableFuture.supplyAsync( () ->  {
            try {
                buildTask.setStatus(BuildStatus.BUILD_ENV_DESTROYING);
                buildResult.getRunningEnvironment().destroyEnvironment();
                buildTask.setStatus(BuildStatus.BUILD_ENV_DESTROYED);
                return buildResult;
            } catch (EnvironmentDriverException e) {
                throw new CoreExceptionWrapper(e);
            }
        }, executor);
    }
    
    private CompletableFuture<Boolean> storeResults(BuildTask buildTask, BuildResult buildResult,
            RunningEnvironmentWrapper backupRunningEnvironment, Throwable e) {
        return CompletableFuture.supplyAsync( () ->  {
            boolean completedOk = false;
            try {
                if (buildResult != null) {
                    buildTask.setStatus(BuildStatus.STORING_RESULTS);
                    datastoreAdapter.storeResult(buildTask, buildResult);
                    completedOk = true;
                } else {
                    datastoreAdapter.storeResult(buildTask, e);
                    if(backupRunningEnvironment.getRunningEnvironment() != null)
                        backupRunningEnvironment.getRunningEnvironment().destroyEnvironment();
                    completedOk = false;
                }
            } catch (DatastoreException de) {
                log.errorf(e, "Error storing results of build configuration: %s to datastore.", buildTask.getId());
            } 
            catch (EnvironmentDriverException envE) {
                log.warn("Running environment" + backupRunningEnvironment.getRunningEnvironment() 
                        +  " couldn't be destroyed!", envE);
            }
            finally {
                buildTask.setStatus(BuildStatus.DONE);
                buildTasks.remove(buildTask);
            }
            return completedOk;
        }, executor);
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
    
    /**
     * Dummy wrapper for interface RunningEnvironment
     * @author Jakub Bartecek <jbartece@redhat.com>
     *
     */
    private class RunningEnvironmentWrapper {
        
        private RunningEnvironment runningEnvironment;
        
        public RunningEnvironment getRunningEnvironment() {
            return runningEnvironment;
        }
        
        public void setRunningEnvironment(RunningEnvironment runningEnvironment) {
            this.runningEnvironment = runningEnvironment;
        }
    }


}
