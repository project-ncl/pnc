package org.jboss.pnc.jenkinsbuilddriver;

import com.offbytwo.jenkins.model.BuildResult;
import org.jboss.pnc.model.BuildDriverStatus;

/**
* Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-01.
*/
class BuildStatusAdapter {
    private BuildResult buildResult;

    BuildStatusAdapter(BuildResult buildResult) {
        this.buildResult = buildResult;
    }

    BuildDriverStatus getBuildStatus() {
        switch (buildResult) {
            case FAILURE:
                return BuildDriverStatus.FAILED;
            case UNSTABLE:
                return BuildDriverStatus.UNSTABLE;
            case REBUILDING:
            case BUILDING:
                return BuildDriverStatus.BUILDING;
            case ABORTED:
                return BuildDriverStatus.ABORTED;
            case SUCCESS:
                return BuildDriverStatus.SUCCESS;
            default:
                return BuildDriverStatus.UNKNOWN;
        }
    }
}
