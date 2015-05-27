/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
    COLLECTING_RESULTS_FROM_REPOSITORY_NAMAGER,

    BUILD_ENV_DESTROYING,
    BUILD_ENV_DESTROYED,
    STORING_RESULTS,

    /** Last build status which is set
     *  after storing to db and
     *  just before dropping from list of running builds.
     *  Used to signal via callback that the build is going to be dropped from queue.
     */
    DONE(true),

    /**
     * Missing configuration, un-satisfied dependencies, dependencies failed to build.
     * Rejected can be set before adding to the list of running builds or before dropping form list of running builds.
     */
    REJECTED(true),

    SYSTEM_ERROR(true);

    private boolean isFinal;

    BuildStatus() {
        isFinal = false;
    }

    BuildStatus(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public boolean isCompleted() {
        return this.isFinal;
    }

}
