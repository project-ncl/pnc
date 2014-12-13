package org.jboss.pnc.spi.builddriver;

import org.jboss.pnc.model.BuildStatus;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-13.
 */
public class BuildDriverResult {
    private BuildStatus buildStatus;
    private String consoleOutput;

    public void setBuildStatus(BuildStatus buildStatus) {
        this.buildStatus = buildStatus;
    }

    public BuildStatus getBuildStatus() {
        return buildStatus;
    }

    public void setConsoleOutput(String consoleOutput) {
        this.consoleOutput = consoleOutput;
    }

    public String getConsoleOutput() {
        return consoleOutput;
    }
}
