package org.jboss.pnc.jenkinsbuilddriver;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.module.JenkinsBuildDriverModuleConfig;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConfiguration;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@ApplicationScoped
public class JenkinsBuildDriver implements BuildDriver {

    public static final String DRIVER_ID = "jenkins-build-driver";

    @Inject
    Configuration<JenkinsBuildDriverModuleConfig> configuration;

    private static final Logger log = Logger.getLogger(JenkinsBuildDriver.class);


    private JenkinsServerFactory jenkinsServerFactory;
    private JenkinsBuildMonitor jenkinsBuildMonitor;

    JenkinsBuildDriver() {}

    @Inject
    JenkinsBuildDriver(JenkinsServerFactory jenkinsServerFactory, JenkinsBuildMonitor jenkinsBuildMonitor) {
        this.jenkinsServerFactory = jenkinsServerFactory;
        this.jenkinsBuildMonitor = jenkinsBuildMonitor;
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
    public RunningBuild startProjectBuild(BuildConfiguration buildConfiguration, RepositoryConfiguration repositoryConfiguration) throws BuildDriverException {
        BuildJob build = new BuildJob(jenkinsServerFactory.getJenkinsServer(), buildConfiguration);
        boolean configured = build.configure(repositoryConfiguration, true);
        if (!configured) {
            throw new AssertionError("Cannot configure build job.");
        }
        int buildNumber = build.start();
        log.infof("Started jenkins job %s #%s.", build.getJobName(), buildNumber);
        return new JenkinsRunningBuild(repositoryConfiguration, jenkinsServerFactory, jenkinsBuildMonitor, build);
    }


}
