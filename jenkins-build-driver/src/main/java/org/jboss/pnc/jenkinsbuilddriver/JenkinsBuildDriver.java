package org.jboss.pnc.jenkinsbuilddriver;

import java.util.function.Consumer;

import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.ProjectBuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.repositorymanager.Repository;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
// TODO implement me
public class JenkinsBuildDriver implements BuildDriver {
    private Consumer<ProjectBuildResult> onBuildComplete;

    @Override
    public String getDriverId() {
        return null;
    }

    @Override
    public void setDeployRepository(Repository deployRepository) {
        // TODO: This should probably use the deployment repository
        // embedded in a settings.xml (See below) and alter the command
        // line to use that new deployment repository or set
        // MAVEN_OPTS="-DaltDeploymentRepository=xxxx in the environment
        // of the Jenkins job (where xxxx is the new deployment repostory)
    }

    @Override
    public void setSourceRepository(Repository repositoryProxy) {
        // TODO: This should probably create a settings.xml file with an override
        // so that Maven uses the proxy URL instead.
    }

    @Override
    public void startProjectBuild(ProjectBuildConfiguration projectBuildConfiguration,
            Consumer<ProjectBuildResult> onBuildComplete) {

        this.onBuildComplete = onBuildComplete;

        return;
    }

    @Override
    public boolean canBuild(BuildType buildType) {
        return BuildType.JAVA.equals(buildType);
    }

    // TODO
    private void notifyBuildComplete() {
        // notify build complete

        onBuildComplete.accept(new ProjectBuildResult());
    }

}
