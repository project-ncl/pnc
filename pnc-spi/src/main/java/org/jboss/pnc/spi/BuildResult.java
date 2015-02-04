package org.jboss.pnc.spi;

import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-02-02.
 */
public interface BuildResult {
    BuildDriverResult getBuildDriverResult();

    /**
     * @return Note that RepositoryManagerResult can return nul if build was not successful completed.
     */
    RepositoryManagerResult getRepositoryManagerResult();
}
