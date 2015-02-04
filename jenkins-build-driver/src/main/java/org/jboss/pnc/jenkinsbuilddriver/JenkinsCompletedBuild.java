package org.jboss.pnc.jenkinsbuilddriver;

import org.jboss.pnc.model.BuildDriverStatus;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConfiguration;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-23.
 */
class JenkinsCompletedBuild implements CompletedBuild {

    private JenkinsServerFactory jenkinsServerFactory;
    private BuildJob buildJob;
    private RepositoryConfiguration repositoryConfiguration;
    private BuildDriverStatus buildDriverStatus;

    JenkinsCompletedBuild(JenkinsServerFactory jenkinsServerFactory, BuildJob buildJob, RepositoryConfiguration repositoryConfiguration, BuildDriverStatus buildDriverStatus) {
        this.jenkinsServerFactory = jenkinsServerFactory;
        this.buildJob = buildJob;
        this.repositoryConfiguration = repositoryConfiguration;
        this.buildDriverStatus = buildDriverStatus;
    }

    @Override
    public BuildDriverStatus getCompleteStatus() {
        return buildDriverStatus;
    }

    @Override
    public BuildDriverResult getBuildResult() {
        return new JenkinsBuildResult(jenkinsServerFactory, buildJob, repositoryConfiguration);
    }

    @Override
    public RepositoryConfiguration getRepositoryConfiguration() {
        return repositoryConfiguration;
    }
}
