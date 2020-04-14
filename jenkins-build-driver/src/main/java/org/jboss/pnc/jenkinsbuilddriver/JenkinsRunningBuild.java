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
import org.jboss.pnc.spi.builddriver.BuildDriverStatus;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.environment.RunningEnvironment;

import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-23.
 */
class JenkinsRunningBuild implements RunningBuild {
    private RunningEnvironment runningEnvironment;
    private JenkinsServerFactory jenkinsServerFactory;
    private JenkinsBuildMonitor jenkinsBuildMonitor;
    private BuildJob buildJob;

    public static final Logger log = Logger.getLogger(JenkinsRunningBuild.class);

    public JenkinsRunningBuild(RunningEnvironment runningEnvironment, JenkinsServerFactory jenkinsServerFactory, 
            JenkinsBuildMonitor jenkinsBuildMonitor, BuildJob buildJob) {
        this.runningEnvironment = runningEnvironment;
        this.jenkinsServerFactory = jenkinsServerFactory;
        this.jenkinsBuildMonitor = jenkinsBuildMonitor;
        this.buildJob = buildJob;
    }

    @Override
    public void monitor(Consumer<CompletedBuild> onComplete, Consumer<Throwable> onError) {
        Consumer<BuildDriverStatus> onBuildComplete = (buildDriverStatus) -> {
            onComplete.accept(new JenkinsCompletedBuild(jenkinsServerFactory, buildJob, runningEnvironment, buildDriverStatus));
        };
        Consumer<Throwable> onBuildError = (e) -> {
            onError.accept(e);
        };
        jenkinsBuildMonitor.monitor(buildJob.getJobName(), buildJob.getBuildNumber(), 
                onBuildComplete, onBuildError, runningEnvironment.getJenkinsUrl());
        log.infof("Waiting jenkins job %s #%s to complete.", buildJob.getJobName(), buildJob.getBuildNumber());
    }

    @Override
    public RunningEnvironment getRunningEnvironment() {
        return runningEnvironment;
    }
}
