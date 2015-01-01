package org.jboss.pnc.jenkinsbuilddriver;

import org.jboss.pnc.model.BuildDriverStatus;
import org.jboss.pnc.spi.builddriver.BuildResult;
import org.jboss.pnc.spi.builddriver.CompletedBuild;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-23.
 */
class JenkinsCompletedBuild implements CompletedBuild {

    private JenkinsServerFactory jenkinsServerFactory;
    private BuildJob buildJob;
    private BuildDriverStatus buildDriverStatus;

    JenkinsCompletedBuild(JenkinsServerFactory jenkinsServerFactory, BuildJob buildJob, BuildDriverStatus buildDriverStatus) {
        this.jenkinsServerFactory = jenkinsServerFactory;
        this.buildJob = buildJob;
        this.buildDriverStatus = buildDriverStatus;
    }

    @Override
    public BuildDriverStatus getCompleteStatus() {
        return buildDriverStatus;
    }

    @Override
    public BuildResult getBuildResult() {
        return new JenkinsBuildResult(jenkinsServerFactory, buildJob);
    }

}
