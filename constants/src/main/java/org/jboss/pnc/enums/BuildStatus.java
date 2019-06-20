/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.enums;

import java.util.Arrays;

/**
 * Status of a running or completed build.
 *
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public enum BuildStatus {
    /**
     * Build completed successfully
     */
    SUCCESS (true),

    /**
     * Build failed
     */
    FAILED,

    /**
     * A build has been requested (possibly via dependencies) but no actual build happened as it was
     * not required (no updates).
     */
    NO_REBUILD_REQUIRED (true),

    /**
     * Build is waiting for dependencies to finish
     */
    WAITING_FOR_DEPENDENCIES,

    /**
     * Build currently running
     */
    BUILDING,

    /**
     * Build rejected due to conflict with another build, or failed dependency build
     */
    REJECTED,

    /**
     * User cancelled the build
     */
    CANCELLED,

    /**
     * A system error prevented the build from completing
     */
    SYSTEM_ERROR,

    /**
     * It is not known what the build status is at this time
     */
    NEW;

    private final boolean completedSuccessfully;

    BuildStatus() {
        this(false);
    }

    BuildStatus(boolean completedSuccessfully) {
        this.completedSuccessfully = completedSuccessfully;
    }

    public boolean completedSuccessfully() {
        return completedSuccessfully;
    }

    @Deprecated
    public static BuildStatus fromBuildCoordinationStatus(BuildCoordinationStatus buildCoordinationStatus) {
        BuildCoordinationStatus[] success = {BuildCoordinationStatus.DONE};
        BuildCoordinationStatus[] failed = {BuildCoordinationStatus.DONE_WITH_ERRORS};
        BuildCoordinationStatus[] cancelled = {BuildCoordinationStatus.CANCELLED};
        BuildCoordinationStatus[] building = {BuildCoordinationStatus.NEW,
                BuildCoordinationStatus.ENQUEUED, BuildCoordinationStatus.BUILDING,
                BuildCoordinationStatus.BUILD_COMPLETED};
        BuildCoordinationStatus[] waitingForDependencies = {BuildCoordinationStatus.WAITING_FOR_DEPENDENCIES};
        BuildCoordinationStatus[] notRequired = {BuildCoordinationStatus.REJECTED_ALREADY_BUILT};
        BuildCoordinationStatus[] rejected = { BuildCoordinationStatus.REJECTED_FAILED_DEPENDENCIES, BuildCoordinationStatus.REJECTED};

        if (Arrays.asList(success).contains(buildCoordinationStatus)) {
            return SUCCESS;
        } else if (Arrays.asList(failed).contains(buildCoordinationStatus)) {
            return FAILED;
        } else if (Arrays.asList(cancelled).contains(buildCoordinationStatus)) {
            return CANCELLED;
        } else if (Arrays.asList(building).contains(buildCoordinationStatus)) {
            return BUILDING;
        } else if (Arrays.asList(waitingForDependencies).contains(buildCoordinationStatus)) {
            return WAITING_FOR_DEPENDENCIES;
        } else if (Arrays.asList(notRequired).contains(buildCoordinationStatus)) {
            return NO_REBUILD_REQUIRED;
        } else if (Arrays.asList(rejected).contains(buildCoordinationStatus)) {
            return REJECTED;
        } else {
            return SYSTEM_ERROR;
        }
    }
}
