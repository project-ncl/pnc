package org.jboss.pnc.jenkinsbuilddriver;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.JobWithDetails;
import org.jboss.pnc.model.BuildDriverStatus;
import org.jboss.pnc.spi.builddriver.BuildResult;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;

import java.io.IOException;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-23.
 */
class JenkinsBuildResult implements BuildResult {

    private final JenkinsServerFactory jenkinsServerFactory;
    private BuildJob buildJob;
    private BuildWithDetails jenkinsBuildDetails = null;

    JenkinsBuildResult(JenkinsServerFactory jenkinsServerFactory, BuildJob buildJob) {
        this.jenkinsServerFactory = jenkinsServerFactory;
        this.buildJob = buildJob;
    }

    @Override
    public String getBuildLog() throws BuildDriverException {
        try {
            return getJenkinsBuildDetails().getConsoleOutputText();
        } catch (IOException e) {
            throw new BuildDriverException("Cannot retrieve build log.", e);
        }
    }

    @Override
    public BuildDriverStatus getBuildDriverStatus() throws BuildDriverException {
        return new BuildStatusAdapter(getJenkinsBuildDetails().getResult()).getBuildStatus();
    }

    private BuildWithDetails getJenkinsBuildDetails() throws BuildDriverException {
        if (jenkinsBuildDetails == null) { //TODO synchronized
            try {
                Build jenkinsBuild = getBuild(jenkinsServerFactory.getJenkinsServer(), buildJob);
                jenkinsBuildDetails = jenkinsBuild.details();
            } catch (IOException e) {
                throw new BuildDriverException("Cannot read jenkins build details.", e);
            }
        }
        return jenkinsBuildDetails;
    }

    private Build getBuild(JenkinsServer jenkinsServer, BuildJob buildJob) throws IOException, BuildDriverException {
        String jobName = buildJob.getJobName();
        JobWithDetails buildJobWithDetails = jenkinsServer.getJob(jobName);
        Build jenkinsBuild = buildJobWithDetails.getLastBuild();
        int buildNumber = jenkinsBuild.getNumber();
        if (buildNumber != buildJob.getBuildNumber()) {
            throw new BuildDriverException("Retrieved wrong build.");
        }
        return jenkinsBuild;
    }

}
