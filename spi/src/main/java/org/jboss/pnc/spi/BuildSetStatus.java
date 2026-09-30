/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi;

import java.util.Arrays;

import org.jboss.pnc.enums.BuildStatus;

/**
 * Status represent the status of the BuildSet has in the BuildCoordinator.
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-05-15.
 */
// mstodo can be removed
public enum BuildSetStatus {
    NEW(false, BuildStatus.NEW),
    DONE(true, BuildStatus.SUCCESS),
    REJECTED(true, BuildStatus.REJECTED),
    /**
     * No build config in the set requires a rebuild.
     */
    NO_REBUILD_REQUIRED(true, BuildStatus.NO_REBUILD_REQUIRED);

    private final boolean isFinal;

    private final BuildStatus buildStatus;

    BuildSetStatus(boolean isFinal, BuildStatus status) {
        this.isFinal = isFinal;
        this.buildStatus = status;
    }

    public boolean isCompleted() {
        return isFinal;
    }

    public BuildStatus buildStatus() {
        return buildStatus;
    }

    public static BuildSetStatus fromBuildStatus(BuildStatus status) {
        return Arrays.stream(BuildSetStatus.values())
                .filter(setStatus -> setStatus.buildStatus().equals(status))
                .findFirst()
                .orElse(NEW);
    }
}
