package org.jboss.pnc.core.builder;

import org.jboss.logging.Logger;
import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.RepositoryManagerFactory;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.core.exception.CoreExceptionWrapper;
import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.BuildDriverStatus;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.BuildResult;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-20.
 */
@ApplicationScoped
public class BuildCoordinator {

    private Logger log = Logger.getLogger(BuildCoordinator.class);
    private Queue<SubmittedBuild> submittedBuilds = new ConcurrentLinkedQueue();

//    @Resource
//    private ManagedThreadFactory threadFactory;
    Executor executor = Executors.newFixedThreadPool(4);

    RepositoryManagerFactory repositoryManagerFactory;
    BuildDriverFactory buildDriverFactory;
    DatastoreAdapter datastoreAdapter;

    public BuildCoordinator(){}

    @Inject
    public BuildCoordinator(BuildDriverFactory buildDriverFactory, RepositoryManagerFactory repositoryManagerFactory, DatastoreAdapter datastoreAdapter) {
        this.buildDriverFactory = buildDriverFactory;
        this.repositoryManagerFactory = repositoryManagerFactory;
        this.datastoreAdapter = datastoreAdapter;
    }

    public SubmittedBuild build(ProjectBuildConfiguration projectBuildConfiguration) throws CoreException {
        SubmittedBuild submittedBuild = new SubmittedBuild(projectBuildConfiguration);
        return prepareBuild(submittedBuild);
    }

    public SubmittedBuild build(ProjectBuildConfiguration projectBuildConfiguration, Set<Consumer<BuildStatus>> statusUpdateListeners, Set<Consumer<String>> logConsumers) throws CoreException {
        SubmittedBuild submittedBuild = new SubmittedBuild(projectBuildConfiguration, statusUpdateListeners, logConsumers);
        return prepareBuild(submittedBuild);
    }

    private SubmittedBuild prepareBuild(SubmittedBuild submittedBuild) throws CoreException {
        if (!isBuildingAlreadySubmitted(submittedBuild)) {
            submittedBuilds.add(submittedBuild);
            submit(submittedBuild);
        } else {
            submittedBuild.setStatus(BuildStatus.REJECTED);
        }
        return submittedBuild;
    }

    private void submit(SubmittedBuild submittedBuild) throws CoreException {
        RepositoryManager repositoryManager = repositoryManagerFactory.getRepositoryManager(RepositoryType.MAVEN);
        BuildDriver buildDriver = buildDriverFactory.getBuildDriver(submittedBuild.getProjectBuildConfiguration().getEnvironment().getBuildType());

        configureRepository(submittedBuild, repositoryManager)
                .thenCompose(repositoryConfiguration -> buildSetUp(submittedBuild, buildDriver, repositoryConfiguration))
                .thenCompose(runningBuild -> waitBuildToComplete(submittedBuild, runningBuild))
                .thenCompose(completedBuild -> retrieveBuildResults(submittedBuild, completedBuild))
                .handle((buildResults, e) -> storeResults(submittedBuild, buildResults, e));
    }

    private CompletableFuture<RepositoryConfiguration> configureRepository(SubmittedBuild submittedBuild, RepositoryManager repositoryManager) {
        return CompletableFuture.supplyAsync( () ->  {
            submittedBuild.setStatus(BuildStatus.REPO_SETTING_UP);
            ProjectBuildConfiguration projectBuildConfiguration = submittedBuild.getProjectBuildConfiguration();
            try {
                return repositoryManager.createRepository(submittedBuild.getProjectBuildConfiguration(), (BuildCollection) null);
            } catch (RepositoryManagerException e) {
                throw new CoreExceptionWrapper(e);
            }
        });
    }

    private CompletableFuture<RunningBuild> buildSetUp(SubmittedBuild submittedBuild, BuildDriver buildDriver, RepositoryConfiguration repositoryConfiguration) {
        return CompletableFuture.supplyAsync( () ->  {
            submittedBuild.setStatus(BuildStatus.BUILD_SETTING_UP);
            ProjectBuildConfiguration projectBuildConfiguration = submittedBuild.getProjectBuildConfiguration();
            try {
                return buildDriver.startProjectBuild(submittedBuild.getProjectBuildConfiguration(), repositoryConfiguration);
            } catch (BuildDriverException e) {
                throw new CoreExceptionWrapper(e);
            }
        });
    }

    private CompletableFuture<CompletedBuild> waitBuildToComplete(SubmittedBuild submittedBuild, RunningBuild runningBuild) {
        CompletableFuture<CompletedBuild> waitToCompleteFuture = new CompletableFuture();
            try {
                Consumer<CompletedBuild> onComplete = (completedBuild) -> {
                    waitToCompleteFuture.complete(completedBuild);
                };
                Consumer<Exception> onError = (e) -> {
                    waitToCompleteFuture.completeExceptionally(e);
                };
                submittedBuild.setStatus(BuildStatus.BUILD_WAITING);

                runningBuild.monitor(onComplete, onError);
            } catch (Throwable throwable) { //TODO narrow down exception
                waitToCompleteFuture.completeExceptionally(throwable);
            }
        return waitToCompleteFuture;
    }

    private CompletionStage<BuildResult> retrieveBuildResults(SubmittedBuild submittedBuild, CompletedBuild completedBuild) {
        return CompletableFuture.supplyAsync( () ->  {
            submittedBuild.setStatus(BuildStatus.COLLECTING_RESULTS);
            ProjectBuildConfiguration projectBuildConfiguration = submittedBuild.getProjectBuildConfiguration();
            try {
                return completedBuild.getBuildResult();
            } catch (BuildDriverException e) {
                throw new CoreExceptionWrapper(e);
            }
        });
    }

    private CompletableFuture<Boolean> storeResults(SubmittedBuild submittedBuild, BuildResult buildResult, Throwable e) {
        return CompletableFuture.supplyAsync( () ->  {
            boolean completedOk = false;
            try {
                try {
                    if (buildResult != null) {

                        BuildDriverStatus buildDriverStatus = buildResult.getBuildDriverStatus();

                        if (buildDriverStatus == BuildDriverStatus.SUCCESS) {
                            submittedBuild.setStatus(BuildStatus.BUILD_COMPLETED_SUCCESS);
                        } else {
                            submittedBuild.setStatus(BuildStatus.BUILD_COMPLETED_WITH_ERROR);
                        }
                        submittedBuild.setStatus(BuildStatus.STORING_RESULTS);
                        datastoreAdapter.storeResult(submittedBuild, buildResult);
                        completedOk = true;
                    } else {
                        datastoreAdapter.storeResult(submittedBuild, e);
                        completedOk = false;
                    }
                } catch (BuildDriverException bde) {
                    submittedBuild.setStatusDescription("Error retrieving build results.");
                    datastoreAdapter.storeResult(submittedBuild, bde);
                }
            } catch (DatastoreException de) {
                log.errorf(e, "Error storing results of build configuration: %s to datastore.", submittedBuild.getIdentifier());
            } finally {
                submittedBuild.setStatus(BuildStatus.DONE);
                submittedBuilds.remove(submittedBuild);
                return completedOk;
            }
        });
    }

    public List<SubmittedBuild> getSubmittedBuilds() {
        return Collections.unmodifiableList(submittedBuilds.stream().collect(Collectors.toList()));
    }

    public SubmittedBuild getBuild(String identifier) {
        List<SubmittedBuild> submittedBuildsFiltered = submittedBuilds.stream()
                .filter(b -> identifier.equals(b.projectBuildConfiguration.getIdentifier())).collect(Collectors.toList());
        return submittedBuildsFiltered.get(0); //TODO validate that there is exactly one ?
    }

    private boolean isBuildingAlreadySubmitted(SubmittedBuild submittedBuild) {
        return submittedBuilds.contains(submittedBuild);
    }


}
