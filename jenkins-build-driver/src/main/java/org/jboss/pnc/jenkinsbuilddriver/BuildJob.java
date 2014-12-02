package org.jboss.pnc.jenkinsbuilddriver;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildResult;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.JobWithDetails;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;

import java.io.IOException;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-29.
 */
public class BuildJob {
    private JenkinsServer jenkinsServer;
    private BuildJobConfig buildJobConfig;
    JobWithDetails job;
    private Build lastBuild;

    public BuildJob(JenkinsServer jenkinsServer) {
        this.jenkinsServer = jenkinsServer;
    }

    public boolean configure(BuildJobConfig buildJobConfig, boolean override) throws BuildDriverException {
        this.buildJobConfig = buildJobConfig;

        try {
            job = jenkinsServer.getJob(buildJobConfig.getName());
        } catch (IOException e) {
            throw new BuildDriverException("Cannot check for existing job.", e);
        }

        try {
            if (job != null) {
                if (override) {
                    jenkinsServer.updateJob(buildJobConfig.getName(), buildJobConfig.getXml());
                } else {
                    //TODO log
                    return false;
                }
            } else {
                jenkinsServer.createJob(buildJobConfig.getName(), buildJobConfig.getXml());
            }
        } catch (IOException e) {
            throw new BuildDriverException("Cannot create/update job.", e);
        }
        try {
            job = jenkinsServer.getJob(buildJobConfig.getName());
        } catch (IOException e) {
            throw new BuildDriverException("Cannot retrieve just created job.", e);
        }
        return job != null;
    }

    public boolean start() throws BuildDriverException {
        //TODO check if configured
        try {
            if (buildJobConfig.getParams() != null) {
                job.build(buildJobConfig.getParams());
            } else {
                job.build();
            }
        } catch (IOException e) {
            throw new BuildDriverException("Cannot start project build.", e);
        }

        //TODO make sure the build was scheduled
        return true;
//        this.lastBuild = job.getLastBuild();
//        return getLastBuildDetails().isBuilding();
    }

    public boolean isRunning() throws BuildDriverException, IOException {
        return getLastBuildDetails().isBuilding();
    }

    public BuildStatus getBuildStatus() throws BuildDriverException, IOException {
        BuildResult buildresult = getLastBuildDetails().getResult();
        BuildStatusAdapter bsa = new BuildStatusAdapter(buildresult);
        return bsa.getBuildStatus();
    }

    public BuildWithDetails getLastBuildDetails() throws BuildDriverException, IOException { //todo wrap io exc
        try {
            job = jenkinsServer.getJob(buildJobConfig.getName());
        } catch (IOException e) {
            throw new BuildDriverException("Cannot check for existing job.", e);
        }
        lastBuild = job.getLastBuild();
        BuildWithDetails lastBuildDetails = lastBuild.details();
        return lastBuildDetails;
    }

}
