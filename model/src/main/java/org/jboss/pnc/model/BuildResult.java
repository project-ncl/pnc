package org.jboss.pnc.model;

import java.util.Set;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public class BuildResult {

    private BuildStatus status;

    /**
     * Driver that was used to run the build.
     */
    private String buildDriverId;

    /**
     * Image that was used to instantiate a build server.
     */
    private String buildImageId;

    private Set<Artifact> artifacts;

    private Set<String> logs;

    public BuildStatus getStatus() {
        return status;
    }
}
