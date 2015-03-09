package org.jboss.pnc.jenkinsbuilddriver;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.JobWithDetails;
import org.jboss.pnc.common.util.StreamCollectors;
import org.jboss.pnc.model.BuildDriverStatus;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
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
    public BuildDriverStatus getBuildDriverStatus() throws BuildDriverException {
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
