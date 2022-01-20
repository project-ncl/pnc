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

/**
 * Represents the status of BuildTask in the coordinator. Status is used in dependency resolution and external status
 * update notification. It is not mean to be used as status of DB entity.
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-22.
 */
public enum BuildCoordinationStatus {
    NEW,

    ENQUEUED,

    WAITING_FOR_DEPENDENCIES,

    BUILDING,

    BUILD_COMPLETED,

    /**
     * Last build status which is set after storing to db and just before dropping from list of running builds. Used to
     * signal via callback that the build is going to be dropped from queue.
     */
    DONE(true),

    /**
     * Missing configuration, un-satisfied dependencies. Rejected can be set before adding to the list of running builds
     * or before dropping form list of running builds.
     */
    REJECTED(true, true),

    /**
     * Rejected due to failed dependencies.
     */
    REJECTED_FAILED_DEPENDENCIES(true, true),

    /**
     * Rejected because given {@link org.jboss.pnc.model.BuildConfiguration} has been already built.
     */
    REJECTED_ALREADY_BUILT(true, false),

    SYSTEM_ERROR(true, true),

    DONE_WITH_ERRORS(true, true),

    CANCELLED(true, true);

    private boolean isFinal;

    private boolean hasFailed = false;

    BuildCoordinationStatus() {
        isFinal = false;
    }

    BuildCoordinationStatus(boolean isFinal) {
        this.isFinal = isFinal;
    }

    BuildCoordinationStatus(boolean isFinal, boolean hasFailed) {
        this.isFinal = isFinal;
        this.hasFailed = hasFailed;
    }

    public boolean isCompleted() {
        return this.isFinal;
    }

    public boolean hasFailed() {
        return this.hasFailed;
    }

    /**
     * do not mix statuses
     */
    @Deprecated
    public static BuildCoordinationStatus fromBuildStatus(BuildStatus buildStatus) { // TODO

        BuildStatus[] done = { BuildStatus.SUCCESS };
        BuildStatus[] doneWithErrors = { BuildStatus.FAILED };
        BuildStatus[] rejected = { BuildStatus.REJECTED };
        BuildStatus[] rejectedFailedDependencies = { BuildStatus.REJECTED_FAILED_DEPENDENCIES };
        BuildStatus[] cancelled = { BuildStatus.CANCELLED };
        BuildStatus[] newBuild = { BuildStatus.NEW };
        BuildStatus[] enqueued = { BuildStatus.ENQUEUED };
        BuildStatus[] building = { BuildStatus.BUILDING };
        BuildStatus[] waitingForDependencies = { BuildStatus.WAITING_FOR_DEPENDENCIES };
        BuildStatus[] notRequired = { BuildStatus.NO_REBUILD_REQUIRED };

        if (Arrays.asList(done).contains(buildStatus)) {
            return DONE;
        } else if (Arrays.asList(doneWithErrors).contains(buildStatus)) {
            return DONE_WITH_ERRORS;
        } else if (Arrays.asList(rejected).contains(buildStatus)) {
            return REJECTED;
        } else if (Arrays.asList(rejectedFailedDependencies).contains(buildStatus)) {
            return REJECTED_FAILED_DEPENDENCIES;
        } else if (Arrays.asList(newBuild).contains(buildStatus)) {
            return NEW;
        } else if (Arrays.asList(enqueued).contains(buildStatus)) {
            return ENQUEUED;
        } else if (Arrays.asList(building).contains(buildStatus)) {
            return BUILDING;
        } else if (Arrays.asList(waitingForDependencies).contains(buildStatus)) {
            return WAITING_FOR_DEPENDENCIES;
        } else if (Arrays.asList(cancelled).contains(buildStatus)) {
            return CANCELLED;
        } else if (Arrays.asList(notRequired).contains(buildStatus)) {
            return REJECTED_ALREADY_BUILT;
        } else {
            return SYSTEM_ERROR;
        }
    }

    /**
     * do not mix statuses
     */
    @Deprecated
    public static BuildCoordinationStatus fromBuildExecutionStatus(BuildExecutionStatus status) { // TODO
        if (status.equals(BuildExecutionStatus.SYSTEM_ERROR)) {
            return SYSTEM_ERROR;
        }

        if (status.isCompleted()) {
            if (status.hasFailed()) {
                return DONE_WITH_ERRORS;
            } else {
                return DONE;
            }
        } else {
            return BUILDING;
        }
    }

}
