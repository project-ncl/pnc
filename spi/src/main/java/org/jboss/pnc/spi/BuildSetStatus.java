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
package org.jboss.pnc.spi;

import org.jboss.pnc.enums.BuildStatus;

import java.util.Arrays;

/**
 * Status represent the status of the BuildSet has in the BuildCoordinator.
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-05-15.
 */
// mstodo can be removed
public enum BuildSetStatus {
    NEW,
    DONE(true, BuildStatus.SUCCESS),
    REJECTED(true, BuildStatus.REJECTED),
    /**
     * No build config in the set requires a rebuild.
     */
    NO_REBUILD_REQUIRED(true, BuildStatus.NO_REBUILD_REQUIRED);

    private final boolean isFinal;

    private final BuildStatus buildStatus;

    BuildSetStatus() {
        isFinal = false;
        buildStatus = BuildStatus.NEW;
    }

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
