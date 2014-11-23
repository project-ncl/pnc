package org.jboss.pnc.jenkinsbuilddriver;

import org.jboss.pnc.core.spi.builddriver.BuildDriver;
import org.jboss.pnc.core.spi.repositorymanager.Repository;
import org.jboss.pnc.model.BuildResult;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.Project;

import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
//TODO implement me
public class JenkinsBuildDriver implements BuildDriver {
    @Override
    public String getDriverId() {
        return null;
    }

    @Override
    public void setDeployRepository(Repository deployRepository) {

    }

    @Override
    public void setSourceRepository(Repository repositoryProxy) {

    }

    @Override
    public void buildProject(Project project, Consumer<BuildResult> onBuildComplete) {
        //TODO
        onBuildComplete.accept(new BuildResult());
    }

    @Override
    public BuildType getBuildType() {
        return BuildType.JAVA;
    }

}
