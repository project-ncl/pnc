package org.jboss.pnc.jenkinsbuilddriver;

import org.jboss.logging.Logger;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@ApplicationScoped
public class JenkinsBuildDriver implements BuildDriver {

    public static final String DRIVER_ID = "jenkins-build-driver";

    @Inject
    Configuration configuration;

    private static final Logger log = Logger.getLogger(JenkinsBuildDriver.class);


    JenkinsServerFactory jenkinsServerFactory;
    JenkinsBuildMonitor jenkinsBuildMonitor;

    ExecutorService executor;

    JenkinsBuildDriver() {}

    @Inject
    JenkinsBuildDriver(JenkinsServerFactory jenkinsServerFactory, JenkinsBuildMonitor jenkinsBuildMonitor) { //TODO
        this.jenkinsServerFactory = jenkinsServerFactory;
        this.jenkinsBuildMonitor = jenkinsBuildMonitor;

        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
        //Jenkins IO thread pool
        executor = new ThreadPoolExecutor(4, 4, 1, TimeUnit.HOURS, workQueue); //TODO configurable

        //TODO executor shutdown
        //TODO jenkinsBuildMonitor shutdown

    }

    @Override
    public String getDriverId() {
        return DRIVER_ID;
    }

    @Override
    public boolean canBuild(BuildType buildType) {
        return BuildType.JAVA.equals(buildType);
    }

    @Override
    public RunningBuild startProjectBuild(ProjectBuildConfiguration projectBuildConfiguration, RepositoryConfiguration repositoryConfiguration) throws BuildDriverException {
        BuildJob build = new BuildJob(jenkinsServerFactory.getJenkinsServer(), projectBuildConfiguration);
        boolean configured = build.configure(repositoryConfiguration, true);
        if (!configured) {
            throw new AssertionError("Cannot configure build job.");
        }
        int buildNumber = build.start();
        log.infof("Started jenkins job %s #%s.", build.getJobName(), buildNumber);
        return new JenkinsRunningBuild(jenkinsServerFactory, jenkinsBuildMonitor, build);
    }


}
