package org.jboss.pnc.spi.builddriver;

import org.jboss.pnc.model.BuildStatus;

/**
 * List of Jenkins job statuses.
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public enum BuildDriverStatus {
    SUCCESS, FAILED, UNSTABLE, BUILDING, ABORTED, CANCELLED, UNKNOWN;

    /**
     * Converts BuildDriverStatus to BuildStatus
     * 
     * @return Corresponding BuildStatus
     */
    public BuildStatus toBuildStatus() {
        switch (this) {
            case SUCCESS:
                return BuildStatus.SUCCESS;
            case FAILED:
                return BuildStatus.FAILED;
            case UNSTABLE:
                return BuildStatus.UNSTABLE;
            case BUILDING:
                return BuildStatus.BUILDING;
            case ABORTED:
                return BuildStatus.ABORTED;
            case CANCELLED:
                return BuildStatus.CANCELLED;
            case UNKNOWN:
                return BuildStatus.UNKNOWN;
            default:
                throw new IllegalStateException("Bad design of BuildDriverStatus enum type");

        }
    }
}
