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
package org.jboss.pnc.spi;

import org.jboss.pnc.enums.RebuildMode;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Class used to store all available build options of a BuildConfiguration or BuildConfigurationSet
 *
 * @author Jakub Bartecek
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Getter
@Setter
public class BuildOptions {

    /**
     * Temporary build or standard build?
     */
    private boolean temporaryBuild = false;

    /**
     * Should we build also dependencies of this BuildConfiguration? Valid only for BuildConfiguration
     */
    private boolean buildDependencies = true;

    /**
     * Should we keep the build container running, if the build fails? Valid only for BuildConfiguration
     */
    private boolean keepPodOnFailure = false;

    /**
     * Should we add a timestamp during the alignment?
     */
    private boolean timestampAlignment = false;

    private RebuildMode rebuildMode = RebuildMode.IMPLICIT_DEPENDENCY_CHECK;

    public boolean isImplicitDependenciesCheck() {
        return RebuildMode.IMPLICIT_DEPENDENCY_CHECK.equals(rebuildMode);
    }

    public boolean isForceRebuild() {
        return RebuildMode.FORCE.equals(rebuildMode);
    }

    @Deprecated
    public boolean isTimestampAlignment() {
        return false;
    }
}
