package org.jboss.pnc.spi.builddriver;

import org.jboss.pnc.spi.repositorymanager.model.RepositoryConfiguration;

import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-23.
 */
public interface RunningBuild {
    void monitor(Consumer<CompletedBuild> onComplete, Consumer<Exception> onError);

    RepositoryConfiguration getRepositoryConfiguration();
}
