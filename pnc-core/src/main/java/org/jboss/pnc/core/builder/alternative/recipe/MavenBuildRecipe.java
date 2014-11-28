package org.jboss.pnc.core.builder.alternative.recipe;

import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.RepositoryManagerFactory;
import org.jboss.pnc.model.BuildResult;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.RepositoryManagerType;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.environment.EnvironmentDriver;
import org.jboss.pnc.spi.environment.EnvironmentDriverProvider;
import org.jboss.pnc.spi.repositorymanager.Repository;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;

import javax.inject.Inject;
import java.util.function.Function;
import java.util.logging.Logger;


public class MavenBuildRecipe implements Function<Project, BuildResult> {

    @Inject
    BuildDriverFactory buildDriverFactory;

    @Inject
    RepositoryManagerFactory repositoryManagerFactory;

    @Inject
    Datastore datastore;

    @Inject
    EnvironmentDriverProvider environmentDriverProvider;

    @Inject
    private Logger log;

    @Override
    public BuildResult apply(Project project) {
        BuildResult result = new BuildResult();
        try {
            BuildDriver buildDriver = buildDriverFactory.getBuildDriver(project.getEnvironment().getBuildType());
            RepositoryManager repositoryManager = repositoryManagerFactory.getRepositoryManager(RepositoryManagerType.MAVEN); //TODO configure per project

            Repository deployRepository = repositoryManager.createEmptyRepository();
            Repository repositoryProxy = repositoryManager.createProxyRepository();

            buildDriver.setDeployRepository(deployRepository);
            buildDriver.setSourceRepository(repositoryProxy);

            EnvironmentDriver environmentDriver = environmentDriverProvider.getDriver(project.getEnvironment().getOperationalSystem());
            environmentDriver.buildEnvironment(project.getEnvironment());

            buildDriver.startProjectBuild(project, (BuildResult) -> result.setStatus(BuildStatus.SUCCESS));
        } catch (Exception e) {
            result.setStatus(BuildStatus.FAILED);
        }
        return result;
    }
}
