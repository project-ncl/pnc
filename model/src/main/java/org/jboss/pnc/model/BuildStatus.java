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
package org.jboss.pnc.model;

import javax.persistence.Transient;

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
     * A build has been requested (possibly via dependencies) but no actual build happened as it was not required (no updates).
     */
    NO_REBUILD_REQUIRED (true),

    /**
     * Build completed with test failures
     */
    UNSTABLE (true),

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
    UNKNOWN, 

    /**
     * There have not been any builds of this configuration, essentially the same meaning
     * as a null status
     */
    NONE;

    private final boolean completedSuccessfully;

    BuildStatus() {
        this.completedSuccessfully = false;
    }

    BuildStatus(boolean completedSuccessfully) {
        this.completedSuccessfully = completedSuccessfully;
    }

    @Transient
    public boolean completedSuccessfully() {
        return completedSuccessfully;
    }
}
