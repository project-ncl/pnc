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
import com.offbytwo.jenkins.model.JobWithDetails;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-29.
 */
class BuildJob {
    private JenkinsServer jenkinsServer;
    private BuildConfigurationAudited buildConfiguration;
    private BuildJobConfig buildJobConfig;
    private JobWithDetails job;
    private int buildNumber;
    
    private boolean crumbflag;
    
    private static final Logger log = Logger.getLogger(BuildJob.class.getName());

    public BuildJob(JenkinsServer jenkinsServer, BuildConfigurationAudited buildConfiguration) {
        this.jenkinsServer = jenkinsServer;
        this.buildConfiguration = buildConfiguration;
    }

    public boolean configure(RunningEnvironment runningEnvironment, boolean override, boolean crumbFlag) throws BuildDriverException {
        String jobName = getJobName();
        
        // is jenkins using Default Crumb Issuer - Prevent Cross Site Request Forgery exploits
        this.crumbflag = crumbFlag;
        
        this.buildJobConfig = new BuildJobConfig(
                jobName,
                buildConfiguration.getScmRepoURL(),
                buildConfiguration.getScmRevision(),
                buildConfiguration.getBuildScript());

        try {
            job = jenkinsServer.getJob(jobName);
        } catch (IOException e) {
            throw new BuildDriverException("Cannot check for existing job.", e);
        }

        try {
            if (job != null) {
                if (override) {
                    jenkinsServer.updateJob(jobName, buildJobConfig.getXml(), crumbflag);
                } else {
                    throw new BuildDriverException("Cannot update existing job without 'override=true'."); 
                }
            } else {
                log.info("job config:\n" + buildJobConfig.getXml());
                jenkinsServer.createJob(jobName, buildJobConfig.getXml(), crumbflag);
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
            job.build(crumbflag);
        } catch (IOException e) {
            throw new BuildDriverException("Cannot start project build.", e);
        }

        return buildNumber;
    }

    public int getBuildNumber() {
        return buildNumber;
    }
}
