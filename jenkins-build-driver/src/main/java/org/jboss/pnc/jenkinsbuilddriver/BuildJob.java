package org.jboss.pnc.jenkinsbuilddriver;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConfiguration;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.JobWithDetails;

import java.io.IOException;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-29.
 */
class BuildJob {
    private JenkinsServer jenkinsServer;
    private BuildConfiguration buildConfiguration;
    private BuildJobConfig buildJobConfig;
    private JobWithDetails job;
    private int buildNumber;

    public BuildJob(JenkinsServer jenkinsServer, BuildConfiguration buildConfiguration) {
        this.jenkinsServer = jenkinsServer;
        this.buildConfiguration = buildConfiguration;
    }

    public boolean configure(RepositoryConfiguration repositoryConfiguration, boolean override) throws BuildDriverException {
        String jobName = getJobName();

        this.buildJobConfig = new BuildJobConfig(
                jobName,
                buildConfiguration.getScmUrl(),
                buildConfiguration.getScmBranch(),
                buildConfiguration.getBuildScript(),
                repositoryConfiguration.getConnectionInfo());

        try {
            job = jenkinsServer.getJob(jobName);
        } catch (IOException e) {
            throw new BuildDriverException("Cannot check for existing job.", e);
        }

        try {
            if (job != null) {
                if (override) {
                    jenkinsServer.updateJob(jobName, buildJobConfig.getXml());
                } else {
                    //TODO log
                    return false;
                }
            } else {
                jenkinsServer.createJob(jobName, buildJobConfig.getXml());
            }
        } catch (IOException e) {
            throw new BuildDriverException("Cannot create/update job.", e);
        }
        try {
            job = jenkinsServer.getJob(jobName);
        } catch (IOException e) {
            throw new BuildDriverException("Cannot retrieve just created job.", e);
        }
        return job != null;
    }

    public String getJobName() {
        return buildConfiguration.getName();
    }

    public int start() throws BuildDriverException {
        //TODO check if configured
        buildNumber = -1;
        try {
            buildNumber = job.getNextBuildNumber();
            job.build();
        } catch (IOException e) {
            throw new BuildDriverException("Cannot start project build.", e);
        }

        return buildNumber;
    }

    public int getBuildNumber() {
        return buildNumber;
    }
}
