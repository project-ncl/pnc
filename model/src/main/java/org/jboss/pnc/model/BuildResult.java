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

    private Set<String> logs;

    public BuildStatus getStatus() {
        return status;
    }

    /**
     * @return List of artifacts that were build by task producing this result.
     */
    public Set<Artifact> getBuildArtifacts() {
        return null;//TODO
    }

    /**
     * @return List of artifacts that were required by this build task.
     * Artifacts can be from internal repo (already build) or imported.
     */
    public Set<Artifact> getDependencies() {
        return null;//TODO
    }
}
