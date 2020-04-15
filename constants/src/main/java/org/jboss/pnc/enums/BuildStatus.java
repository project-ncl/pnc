/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import static org.jboss.pnc.enums.BuildProgress.FINISHED;
import static org.jboss.pnc.enums.BuildProgress.IN_PROGRESS;
import static org.jboss.pnc.enums.BuildProgress.PENDING;

/**
 * Status of a running or isFinal build.
 *
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public enum BuildStatus {
    /**
     * Build completed successfully
     */
    SUCCESS(FINISHED, true),

    /**
     * Build failed
     */
    FAILED(FINISHED, false),

    /**
     * A build has been requested (possibly via dependencies) but no actual build happened as it was not required (no
     * updates).
     */
    NO_REBUILD_REQUIRED(FINISHED, true),

    /**
     * A build has been placed in a queue. It will be processed shortly.
     */
    ENQUEUED(PENDING),

    /**
     * Build is waiting for dependencies to finish
     */
    WAITING_FOR_DEPENDENCIES(PENDING),

    /**
     * Build currently running
     */
    BUILDING(IN_PROGRESS),

    /**
     * Build rejected due to conflict with another build
     */
    REJECTED(FINISHED, false),

    /**
     * Build rejected due to failed dependency build
     */
    REJECTED_FAILED_DEPENDENCIES(FINISHED, false),

    /**
     * User cancelled the build
     */
    CANCELLED(FINISHED, true),

    /**
     * A system error prevented the build from completing
     */
    SYSTEM_ERROR(FINISHED, false),

    /**
     * It is not known what the build status is at this time
     */
    NEW(PENDING);

    private final boolean completedSuccessfully;

    private final BuildProgress progress;

    BuildStatus(BuildProgress progress) {
        this(progress, false);
    }

    private BuildStatus(BuildProgress progress, boolean completedSuccessfully) {
        this.completedSuccessfully = completedSuccessfully;
        this.progress = progress;
    }

    public boolean completedSuccessfully() {
        return completedSuccessfully;
    }

    public boolean isFinal() {
        return progress == FINISHED;
    }

    public BuildProgress progress() {
        return progress;
    }

    @Deprecated
    public static BuildStatus fromBuildCoordinationStatus(BuildCoordinationStatus buildCoordinationStatus) {
        BuildCoordinationStatus[] success = { BuildCoordinationStatus.DONE };
        BuildCoordinationStatus[] failed = { BuildCoordinationStatus.DONE_WITH_ERRORS };
        BuildCoordinationStatus[] cancelled = { BuildCoordinationStatus.CANCELLED };
        BuildCoordinationStatus[] newBuild = { BuildCoordinationStatus.NEW };
        BuildCoordinationStatus[] enqueued = { BuildCoordinationStatus.ENQUEUED };
        BuildCoordinationStatus[] building = { BuildCoordinationStatus.BUILDING,
                BuildCoordinationStatus.BUILD_COMPLETED };
        BuildCoordinationStatus[] waitingForDependencies = { BuildCoordinationStatus.WAITING_FOR_DEPENDENCIES };
        BuildCoordinationStatus[] notRequired = { BuildCoordinationStatus.REJECTED_ALREADY_BUILT };
        BuildCoordinationStatus[] rejected = { BuildCoordinationStatus.REJECTED };
        BuildCoordinationStatus[] rejectedFailedDependencies = { BuildCoordinationStatus.REJECTED_FAILED_DEPENDENCIES };

        if (Arrays.asList(success).contains(buildCoordinationStatus)) {
            return SUCCESS;
        } else if (Arrays.asList(failed).contains(buildCoordinationStatus)) {
            return FAILED;
        } else if (Arrays.asList(cancelled).contains(buildCoordinationStatus)) {
            return CANCELLED;
        } else if (Arrays.asList(newBuild).contains(buildCoordinationStatus)) {
            return NEW;
        } else if (Arrays.asList(enqueued).contains(buildCoordinationStatus)) {
            return ENQUEUED;
        } else if (Arrays.asList(building).contains(buildCoordinationStatus)) {
            return BUILDING;
        } else if (Arrays.asList(waitingForDependencies).contains(buildCoordinationStatus)) {
            return WAITING_FOR_DEPENDENCIES;
        } else if (Arrays.asList(notRequired).contains(buildCoordinationStatus)) {
            return NO_REBUILD_REQUIRED;
        } else if (Arrays.asList(rejected).contains(buildCoordinationStatus)) {
            return REJECTED;
        } else if (Arrays.asList(rejectedFailedDependencies).contains(buildCoordinationStatus)) {
            return REJECTED_FAILED_DEPENDENCIES;
        } else {
            return SYSTEM_ERROR;
        }
    }
}
