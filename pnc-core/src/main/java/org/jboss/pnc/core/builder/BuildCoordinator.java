package org.jboss.pnc.core.builder;

import org.jboss.logging.Logger;
import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.RepositoryManagerFactory;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.core.exception.CoreExceptionWrapper;
import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.BuildDriverStatus;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.BuildResult;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConfiguration;
import org.jboss.util.graph.Edge;
import org.jboss.util.graph.Vertex;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Set;
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
    private Queue<BuildTask> buildTasks = new ConcurrentLinkedQueue(); //TODO garbage collector (time-out, error state)

//    @Resource
//    private ManagedThreadFactory threadFactory;
    private Executor executor = Executors.newFixedThreadPool(4); //TODO configurable

    private RepositoryManagerFactory repositoryManagerFactory;
    private BuildDriverFactory buildDriverFactory;
    private DatastoreAdapter datastoreAdapter;

    @Deprecated
    public BuildCoordinator(){} //workaround for CDI constructor parameter injection

    @Inject
    public BuildCoordinator(BuildDriverFactory buildDriverFactory, RepositoryManagerFactory repositoryManagerFactory, DatastoreAdapter datastoreAdapter) {
        this.buildDriverFactory = buildDriverFactory;
        this.repositoryManagerFactory = repositoryManagerFactory;
        this.datastoreAdapter = datastoreAdapter;
    }

    public BuildTask build(ProjectBuildConfiguration projectBuildConfiguration) throws CoreException {
        return build(projectBuildConfiguration, Collections.emptySet(), Collections.emptySet());
    }

    public BuildTask build(ProjectBuildConfiguration projectBuildConfiguration, Set<Consumer<BuildStatus>> statusUpdateListeners, Set<Consumer<String>> logConsumers) throws CoreException {
        BuildTasksTree buildTasksTree = new BuildTasksTree(this);

        BuildTask buildTask = buildTasksTree.getOrCreateSubmittedBuild(projectBuildConfiguration, statusUpdateListeners, logConsumers);

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
            if (!isConfigurationBuilt(dependentBuildTask.projectBuildConfiguration)) {
                missingDependencies.add(dependentBuildTask);
            }
        }
        return missingDependencies;
    }

    private boolean isConfigurationBuilt(ProjectBuildConfiguration buildConfiguration) {
        return datastoreAdapter.isProjectBuildConfigurationBuilt();
    }

    void startBuilding(BuildTask buildTask) throws CoreException {
        RepositoryManager repositoryManager = repositoryManagerFactory.getRepositoryManager(RepositoryType.MAVEN);
        BuildDriver buildDriver = buildDriverFactory.getBuildDriver(buildTask.getProjectBuildConfiguration().getEnvironment().getBuildType());

        configureRepository(buildTask, repositoryManager)
                .thenCompose(repositoryConfiguration -> buildSetUp(buildTask, buildDriver, repositoryConfiguration))
                .thenCompose(runningBuild -> waitBuildToComplete(buildTask, runningBuild))
                .thenCompose(completedBuild -> retrieveBuildResults(buildTask, completedBuild))
                .handle((buildResults, e) -> storeResults(buildTask, buildResults, e));
    }

    private CompletableFuture<RepositoryConfiguration> configureRepository(BuildTask buildTask, RepositoryManager repositoryManager) {
        return CompletableFuture.supplyAsync( () ->  {
            buildTask.setStatus(BuildStatus.REPO_SETTING_UP);
            ProjectBuildConfiguration projectBuildConfiguration = buildTask.getProjectBuildConfiguration();
            try {

                //TODO remove buildCollection mock
                BuildCollection buildCollection = new BuildCollection();
                ProductVersion productVersion = new ProductVersion();
                productVersion.setVersion("my-product-version");
                Product product = new Product();
                product.setName("my-product");
                productVersion.setProduct(product);
                buildCollection.setProductVersion(productVersion);

                return repositoryManager.createRepository(projectBuildConfiguration, buildCollection);
            } catch (RepositoryManagerException e) {
                throw new CoreExceptionWrapper(e);
            }
        }, executor);
    }

    private CompletableFuture<RunningBuild> buildSetUp(BuildTask buildTask, BuildDriver buildDriver, RepositoryConfiguration repositoryConfiguration) {
        return CompletableFuture.supplyAsync( () ->  {
            buildTask.setStatus(BuildStatus.BUILD_SETTING_UP);
            ProjectBuildConfiguration projectBuildConfiguration = buildTask.getProjectBuildConfiguration();
            try {
                return buildDriver.startProjectBuild(buildTask.getProjectBuildConfiguration(), repositoryConfiguration);
            } catch (BuildDriverException e) {
                throw new CoreExceptionWrapper(e);
            }
        }, executor);
    }

    private CompletableFuture<CompletedBuild> waitBuildToComplete(BuildTask buildTask, RunningBuild runningBuild) {
        CompletableFuture<CompletedBuild> waitToCompleteFuture = new CompletableFuture();
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

    private CompletionStage<BuildResult> retrieveBuildResults(BuildTask buildTask, CompletedBuild completedBuild) {
        return CompletableFuture.supplyAsync( () ->  {
            buildTask.setStatus(BuildStatus.COLLECTING_RESULTS);
            ProjectBuildConfiguration projectBuildConfiguration = buildTask.getProjectBuildConfiguration();
            try {
                return completedBuild.getBuildResult();
            } catch (BuildDriverException e) {
                throw new CoreExceptionWrapper(e);
            }
        }, executor);
    }

    private CompletableFuture<Boolean> storeResults(BuildTask buildTask, BuildResult buildResult, Throwable e) {
        return CompletableFuture.supplyAsync( () ->  {
            boolean completedOk = false;
            try {
                try {
                    if (buildResult != null) {

                        BuildDriverStatus buildDriverStatus = buildResult.getBuildDriverStatus();

                        if (buildDriverStatus == BuildDriverStatus.SUCCESS) {
                            buildTask.setStatus(BuildStatus.BUILD_COMPLETED_SUCCESS);
                        } else {
                            buildTask.setStatus(BuildStatus.BUILD_COMPLETED_WITH_ERROR);
                        }
                        buildTask.setStatus(BuildStatus.STORING_RESULTS);
                        datastoreAdapter.storeResult(buildTask, buildResult);
                        completedOk = true;
                    } else {
                        datastoreAdapter.storeResult(buildTask, e);
                        completedOk = false;
                    }
                } catch (BuildDriverException bde) {
                    buildTask.setStatusDescription("Error retrieving build results.");
                    datastoreAdapter.storeResult(buildTask, bde);
                }
            } catch (DatastoreException de) {
                log.errorf(e, "Error storing results of build configuration: %s to datastore.", buildTask.getId());
            } finally {
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
                .filter(b -> identifier.equals(b.projectBuildConfiguration.getIdentifier())).collect(Collectors.toList());
        return buildsFilteredTask.get(0); //TODO validate that there is exactly one ?
    }

    private boolean isBuildAlreadySubmitted(BuildTask buildTask) {
        return buildTasks.contains(buildTask);
    }


}
