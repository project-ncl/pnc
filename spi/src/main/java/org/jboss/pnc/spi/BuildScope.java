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
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 10/27/16
 * Time: 11:34 AM
 */
public enum BuildScope {
    SINGLE(false, false),
    WITH_DEPENDENCIES(false, true),
    REBUILD(true, false);

    private final boolean forceRebuild;
    private final boolean recursive;

    BuildScope(boolean forceRebuild, boolean recursive) {
        this.forceRebuild = forceRebuild;
        this.recursive = recursive;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public boolean isForceRebuild() {
        return forceRebuild;
    }
}
