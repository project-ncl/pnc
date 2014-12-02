package org.jboss.pnc.jenkinsbuilddriver;

import com.offbytwo.jenkins.model.BuildResult;
import org.jboss.pnc.model.BuildStatus;

/**
* Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-01.
*/
class BuildStatusAdapter {
    private BuildResult buildResult;

    BuildStatusAdapter(BuildResult buildResult) {
        this.buildResult = buildResult;
    }

    BuildStatus getBuildStatus() {
        switch (buildResult) {
            case FAILURE:
                return BuildStatus.FAILED;
            case UNSTABLE:
                return BuildStatus.UNSTABLE;
            case REBUILDING:
                return BuildStatus.REBUILDING;
            case BUILDING:
                return BuildStatus.BUILDING;
            case ABORTED:
                return BuildStatus.ABORTED;
            case SUCCESS:
                return BuildStatus.SUCCESS;
            default:
                return BuildStatus.UNKNOWN;
        }
    }
}
