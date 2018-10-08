/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.JobWithDetails;
import org.jboss.pnc.common.util.StreamCollectors;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.BuildDriverStatus;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;

import java.io.IOException;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-23.
 */
class JenkinsBuildResult implements BuildDriverResult {

    private final JenkinsServerFactory jenkinsServerFactory;
    private final String buildLog;
    private BuildJob buildJob;
    private RunningEnvironment runningEnvironment;
    private BuildWithDetails jenkinsBuildDetails = null;

    JenkinsBuildResult(JenkinsServerFactory jenkinsServerFactory, BuildJob buildJob, RunningEnvironment runningEnvironment) throws BuildDriverException {
        this.jenkinsServerFactory = jenkinsServerFactory;
        this.buildJob = buildJob;
        this.runningEnvironment = runningEnvironment;

        try {
            Build jenkinsBuild = getBuild(jenkinsServerFactory.getJenkinsServer(runningEnvironment.getJenkinsUrl()), buildJob);
            jenkinsBuildDetails = jenkinsBuild.details();
        } catch (IOException e) {
            throw new BuildDriverException("Cannot read jenkins build details.", e);
        }

        try {
            buildLog = jenkinsBuildDetails.getConsoleOutputText();
        } catch (IOException e) {
            throw new BuildDriverException("Cannot retrieve build log.", e);
        }
    }

    @Override
    public String getBuildLog() throws BuildDriverException {
        return buildLog;
    }

    @Override
    public BuildDriverStatus getBuildDriverStatus() {
        return new BuildStatusAdapter(jenkinsBuildDetails.getResult()).getBuildStatus();
    }

    private Build getBuild(JenkinsServer jenkinsServer, BuildJob buildJob) throws IOException, BuildDriverException {
        String jobName = buildJob.getJobName();
        JobWithDetails buildJobWithDetails = jenkinsServer.getJob(jobName);
        Build jenkinsBuild = buildJobWithDetails.getBuilds().stream().filter(j -> buildJob.getBuildNumber() == j.getNumber()).collect(StreamCollectors.singletonCollector());
        int retrievedBuildNumber = jenkinsBuild.getNumber();
        int jobBuildNumber = buildJob.getBuildNumber();
        if (retrievedBuildNumber != jobBuildNumber) {
            throw new BuildDriverException("Retrieved wrong build. Build numbers doesn't match. [retrievedBuildNumber: " + retrievedBuildNumber + " != jobBuildNumber: " + jobBuildNumber + "]");
        }
        return jenkinsBuild;
    }

    @Override
    public RunningEnvironment getRunningEnvironment() {
        return runningEnvironment;
    }
}
