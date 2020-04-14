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

import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.BuildDriverStatus;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-23.
 */
class JenkinsCompletedBuild implements CompletedBuild {

    private JenkinsServerFactory jenkinsServerFactory;
    private BuildJob buildJob;
    private RunningEnvironment runningEnvironment;
    private BuildDriverStatus buildDriverStatus;

    JenkinsCompletedBuild(JenkinsServerFactory jenkinsServerFactory, BuildJob buildJob, RunningEnvironment runningEnvironment, BuildDriverStatus buildDriverStatus) {
        this.jenkinsServerFactory = jenkinsServerFactory;
        this.buildJob = buildJob;
        this.runningEnvironment = runningEnvironment;
        this.buildDriverStatus = buildDriverStatus;
    }

    @Override
    public BuildDriverResult getBuildResult() throws BuildDriverException {
        return new JenkinsBuildResult(jenkinsServerFactory, buildJob, runningEnvironment);
    }

    @Override
    public RunningEnvironment getRunningEnvironment() {
        return runningEnvironment;
    }
}
