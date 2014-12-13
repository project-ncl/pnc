package org.jboss.pnc.model.builder;

import org.jboss.pnc.model.BuildStatus;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-11.
 */
public class BuildDetails {
    private final String jobName;
    private final int buildNumber;
    private String buildLog;
    private BuildStatus buildStatus;

    public BuildDetails(String jobName, int buildNumber) {
        this.jobName = jobName;
        this.buildNumber = buildNumber;
    }

    public int getBuildNumber() {
        return buildNumber;
    }

    public String getJobName() {
        return jobName;
    }

    public void setBuildLog(String buildLog) {
        this.buildLog = buildLog;
    }

    public String getBuildLog() {
        return buildLog;
    }

    public void setBuildStatus(BuildStatus buildStatus) {
        this.buildStatus = buildStatus;
    }

    public BuildStatus getBuildStatus() {
        return buildStatus;
    }
}
