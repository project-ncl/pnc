package org.jboss.pnc.jenkinsbuilddriver;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@ApplicationScoped
public class JenkinsBuildDriver implements BuildDriver {

    public static final String DRIVER_ID = "jenkins-build-driver";

    private static final Logger log = Logger.getLogger(JenkinsBuildDriver.class);


    private JenkinsServerFactory jenkinsServerFactory;
    private JenkinsBuildMonitor jenkinsBuildMonitor;
    
    private boolean isCrumbUsed;

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
    
    public void init(RunningEnvironment runningEnvironment) {
        try {
            this.isCrumbUsed = jenkinsServerFactory.isJenkinsServerSecuredWithCSRF(runningEnvironment.getJenkinsUrl());    
        } catch ( BuildDriverException bde) {
            log.debug("Getting Jenkins Build Driver CSRF setting failed", bde);
        }
    }

    @Override
    public RunningBuild startProjectBuild(BuildConfiguration buildConfiguration, RunningEnvironment runningEnvironment) throws BuildDriverException {
        
        init(runningEnvironment);
        
        BuildJob build = new BuildJob(jenkinsServerFactory.getJenkinsServer(runningEnvironment.getJenkinsUrl()), buildConfiguration);
        boolean configured = build.configure(runningEnvironment, true, isCrumbUsed);
        if (!configured) {
            throw new AssertionError("Cannot configure build job.");
        }
        int buildNumber = build.start();
        log.infof("Started jenkins job %s #%s.", build.getJobName(), buildNumber);
        return new JenkinsRunningBuild(runningEnvironment, jenkinsServerFactory, jenkinsBuildMonitor, build);
    }


}
