package org.jboss.pnc.spi;

/**
* Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-22.
*/
public enum BuildStatus {
    NEW,

    WAITING_FOR_DEPENDENCIES,

    REPO_SETTING_UP,

    BUILD_ENV_SETTING_UP,
    BUILD_ENV_WAITING,
    BUILD_ENV_SETUP_COMPLETE_SUCCESS,
    BUILD_ENV_SETUP_COMPLETE_WITH_ERROR,

    BUILD_SETTING_UP,
    BUILD_WAITING,
    BUILD_COMPLETED_SUCCESS,
    BUILD_COMPLETED_WITH_ERROR,

    COLLECTING_RESULTS_FROM_BUILD_DRIVER,
    BUILD_ENV_DESTROYING,
    BUILD_ENV_DESTROYED,
    STORING_RESULTS,
    RESULTS_STORED,

    /** Last build status which is set
     *  after storing to db and
     *  just before dropping from list of running builds.
     *  Used to signal via callback that the build is going to be dropped from queue.
     */
    DONE,

    /**
     * Missing configuration, un-satisfied dependencies, dependencies failed to build.
     * Rejected can be set before adding to the list of running builds or before dropping form list of running builds.
     */
    REJECTED,

    SYSTEM_ERROR, COLLECTING_RESULTS_FROM_REPOSITORY_NAMAGER;
}
