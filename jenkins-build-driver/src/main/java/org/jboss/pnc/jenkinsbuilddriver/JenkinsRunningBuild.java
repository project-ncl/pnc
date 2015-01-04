package org.jboss.pnc.jenkinsbuilddriver;

import org.jboss.logging.Logger;
import org.jboss.pnc.model.BuildDriverStatus;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;

import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-23.
 */
class JenkinsRunningBuild implements RunningBuild {
    private JenkinsServerFactory jenkinsServerFactory;
    private JenkinsBuildMonitor jenkinsBuildMonitor;
    private BuildJob buildJob;

    public static final Logger log = Logger.getLogger(JenkinsRunningBuild.class);

    public JenkinsRunningBuild(JenkinsServerFactory jenkinsServerFactory, JenkinsBuildMonitor jenkinsBuildMonitor, BuildJob buildJob) {
        this.jenkinsServerFactory = jenkinsServerFactory;
        this.jenkinsBuildMonitor = jenkinsBuildMonitor;
        this.buildJob = buildJob;
    }

    @Override
    public void monitor(Consumer<CompletedBuild> onComplete, Consumer<Exception> onError) {
        Consumer<BuildDriverStatus> onBuildComplete = (buildDriverStatus) -> {
            onComplete.accept(new JenkinsCompletedBuild(jenkinsServerFactory, buildJob, buildDriverStatus));
        };
        Consumer<Exception> onBuildError = (e) -> {
            onError.accept(e);
        };
        jenkinsBuildMonitor.monitor(buildJob.getJobName(), buildJob.getBuildNumber(), onBuildComplete, onBuildError);
        log.infof("Waiting jenkins job %s #%s to complete.", buildJob.getJobName(), buildJob.getBuildNumber());
    }
}
