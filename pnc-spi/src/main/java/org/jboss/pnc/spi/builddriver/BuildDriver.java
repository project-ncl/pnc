package org.jboss.pnc.spi.builddriver;

import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;

import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public interface BuildDriver {

    String getDriverId();

    boolean canBuild(BuildType buildType);

    public void startProjectBuild(ProjectBuildConfiguration projectBuildConfiguration,
                                  RepositoryConfiguration repositoryConfiguration, Consumer<String> onComplete, Consumer<Exception> onError);
}
