/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
import java.util.EnumSet;

import static org.jboss.pnc.enums.BuildProgress.FINISHED;
import static org.jboss.pnc.enums.BuildProgress.IN_PROGRESS;
import static org.jboss.pnc.enums.BuildProgress.PENDING;

/**
 * Status of a running/completed Build/GroupBuild.
 * 
 * The status class is shared by both Build and GroupBuild dtos, which are published through UI/WebSocket.
 *
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 * @deprecated use pnc-api
 */
@Deprecated
public enum BuildStatus {
    /**
     * Build/GroupBuild has completed successfully. The dependant Builds can be scheduled.
     */
    SUCCESS(FINISHED, true),

    /**
     * Build/GroupBuild has failed. The status is propagated through dependants as REJECTED_FAILED_DEPENDENCIES.
     *
     * A GroupBuild is reported FAILED when at least one underlying Build has the final status of (FAILED, REJECTED,
     * SYSTEM_ERROR)
     */
    FAILED(FINISHED, false),

    /**
     * A Build has been requested (possibly via dependants) but no actual build happened as it was not required.
     * 
     * For a previously successful Build, a rebuild is required when (conditions depend on a type of check):
     *
     * **Explicit**
     *
     * 1. The BuildConfig has been modified since the last successful Build
     * 
     * 2. A BuildConfig, explicitly defined as a dependency of this one, has been modified since the last successful
     * Build
     * 
     * 3. There exists a newer, successful Build of a BuildConfig explicitly defined as a dependency of this one.
     *
     *
     * **Implicit**
     *
     * 1. Explicit criteria plus:
     * 
     * 2. There exists a newer version of an implicit dependency which can be: A Build which produced artifacts that
     * were used for alignment during build process or automatically captured from sources such as Indy, MRRC or Maven
     * Central
     *
     *
     * **Force**
     * 
     * 1. Always
     * 
     * A GroupBuild is deemed unnecessary to be built if all the included Builds do not require rebuild.
     *
     *
     */
    NO_REBUILD_REQUIRED(FINISHED, true),

    /**
     * A Build satisfies all necessary requirements to start building process. Build is placed into a queue and waits
     * for a free BuildCoordinator thread to be picked up.
     *
     * With lower number of concurrent Builds, the Build is more likely to wait for available thread.
     */
    ENQUEUED(PENDING),

    /**
     * A Build has unfinished dependencies, therefore waits so it can start building.
     */
    WAITING_FOR_DEPENDENCIES(PENDING),

    /**
     * The Build/GroupBuild building process has started.
     */
    BUILDING(IN_PROGRESS),

    /**
     * Build rejected due to conflict with another build. This happens when there is already a Build with the same
     * BuildConfigRevision in the BuildQueue. (currently this check is done only when the Build is part of GroupBuild)
     *
     * GroupBuild is rejected due to being empty or having cyclic dependencies.
     */
    REJECTED(FINISHED, false),

    /**
     * Build rejected due to failed dependency build.
     */
    REJECTED_FAILED_DEPENDENCIES(FINISHED, false),

    /**
     * User cancelled the Build/GroupBuild. The status is propagated through dependants.
     */
    CANCELLED(FINISHED, false),

    /**
     * A system error prevented the build from completing.
     */
    SYSTEM_ERROR(FINISHED, false),

    /**
     * Initial status of a Build. It is almost immediately changed.
     */
    NEW(PENDING);

    private final boolean completedSuccessfully;

    private final BuildProgress progress;

    private BuildStatus(BuildProgress progress) {
        this(progress, false);
    }

    private BuildStatus(BuildProgress progress, boolean completedSuccessfully) {
        this.completedSuccessfully = completedSuccessfully;
        this.progress = progress;
    }

    public static EnumSet<BuildStatus> unfinished() {
        EnumSet<BuildStatus> result = EnumSet.noneOf(BuildStatus.class);
        for (BuildStatus value : values()) {
            if (value.progress != FINISHED) {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * Returns true if the build finished successfully.
     */
    public boolean completedSuccessfully() {
        return completedSuccessfully;
    }

    /**
     * Returns true if the build finished (successfully or not).
     */
    public boolean isFinal() {
        return progress == FINISHED;
    }

    /**
     * Returns what progress state this status represents.
     * 
     * @see BuildProgress
     */
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
