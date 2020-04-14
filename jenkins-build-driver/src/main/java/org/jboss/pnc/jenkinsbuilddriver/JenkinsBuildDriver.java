/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.jenkinsbuilddriver;

import org.jboss.logging.Logger;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.SystemImageType;
import org.jboss.pnc.spi.BuildExecution;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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

    public void init(RunningEnvironment runningEnvironment) {
        try {
            this.isCrumbUsed = jenkinsServerFactory.isJenkinsServerSecuredWithCSRF(runningEnvironment.getJenkinsUrl());    
        } catch ( BuildDriverException bde) {
            log.debug("Getting Jenkins Build Driver CSRF setting failed", bde);
        }
    }

    @Override
    public RunningBuild startProjectBuild(BuildExecution currentBuildExecution, BuildConfigurationAudited buildConfiguration,
            RunningEnvironment runningEnvironment) throws BuildDriverException {
        
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
