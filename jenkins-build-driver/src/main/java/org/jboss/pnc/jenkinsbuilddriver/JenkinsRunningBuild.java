package org.jboss.pnc.jenkinsbuilddriver;

import org.jboss.logging.Logger;
import org.jboss.pnc.model.BuildDriverStatus;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConfiguration;

import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-23.
 */
class JenkinsRunningBuild implements RunningBuild {
    private RepositoryConfiguration repositoryConfiguration;
    private JenkinsServerFactory jenkinsServerFactory;
    private JenkinsBuildMonitor jenkinsBuildMonitor;
    private BuildJob buildJob;

    public static final Logger log = Logger.getLogger(JenkinsRunningBuild.class);

    public JenkinsRunningBuild(RepositoryConfiguration repositoryConfiguration, JenkinsServerFactory jenkinsServerFactory, JenkinsBuildMonitor jenkinsBuildMonitor, BuildJob buildJob) {
        this.repositoryConfiguration = repositoryConfiguration;
        this.jenkinsServerFactory = jenkinsServerFactory;
        this.jenkinsBuildMonitor = jenkinsBuildMonitor;
        this.buildJob = buildJob;
    }

    @Override
    public void monitor(Consumer<CompletedBuild> onComplete, Consumer<Exception> onError) {
        Consumer<BuildDriverStatus> onBuildComplete = (buildDriverStatus) -> {
            onComplete.accept(new JenkinsCompletedBuild(jenkinsServerFactory, buildJob, repositoryConfiguration, buildDriverStatus));
        };
        Consumer<Exception> onBuildError = (e) -> {
            onError.accept(e);
        };
        jenkinsBuildMonitor.monitor(buildJob.getJobName(), buildJob.getBuildNumber(), onBuildComplete, onBuildError);
        log.infof("Waiting jenkins job %s #%s to complete.", buildJob.getJobName(), buildJob.getBuildNumber());
    }

    @Override
    public RepositoryConfiguration getRepositoryConfiguration() {
        return repositoryConfiguration;
    }
}
