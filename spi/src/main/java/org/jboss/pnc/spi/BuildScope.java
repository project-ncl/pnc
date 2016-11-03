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
 * Scope of a build of a build configuration.
 *
 * <p>
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 10/27/16
 * Time: 11:34 AM
 * </p>
 */
public enum BuildScope {
    /**
     * Build only the build configuration for which the build was triggered.
     */
    SINGLE(false, false),

    /**
     * Build all the dependencies of give build configuration that are not yet built and then build this configuration.
     */
    WITH_DEPENDENCIES(false, true),

    /**
     * Force building this configuration.
     */
    REBUILD(true, false);

    private final boolean forceRebuild;
    private final boolean recursive;

    BuildScope(boolean forceRebuild, boolean recursive) {
        this.forceRebuild = forceRebuild;
        this.recursive = recursive;
    }

    /**
     * Should dependencies of given build configuration be built
     * @return true if the dependencies should be built
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * Should the configuration be rebuilt if it has been built already
     * @return true if the configuration should be rebuilt
     */
    public boolean isForceRebuild() {
        return forceRebuild;
    }
}
